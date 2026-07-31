package lsp;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import choral.diagrams.ChoreographyDiagramException;
import choral.diagrams.ChoreographyDiagramProvider;
import choral.diagrams.ChoreographyDiagramProvider.Position;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import lsp.features.DiagnosticsProvider;
import lsp.features.DiagnosticsProvider.AnalysisFailure;
import lsp.features.DiagnosticsProvider.AnalysisResult;

public class ChoralTextDocumentService implements TextDocumentService {
    private static final Gson GSON = new Gson();
    private final DiagnosticsProvider diagnosticsProvider;
    private final ChoreographyDiagramProvider choreographyDiagramProvider;
    private final Map<String, DocumentState> documents = new ConcurrentHashMap<>();
    private LanguageClient client;
    
    public ChoralTextDocumentService() {
        this(new DiagnosticsProvider());
    }

    public ChoralTextDocumentService(DiagnosticsProvider diagnosticsProvider) {
        this.diagnosticsProvider = Objects.requireNonNull(diagnosticsProvider);
        choreographyDiagramProvider = new ChoreographyDiagramProvider();
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params){
        String uri = params.getTextDocument().getUri();
        String content = params.getTextDocument().getText();

        updateDocument(uri, params.getTextDocument().getVersion(), content);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params){
        String uri = params.getTextDocument().getUri();
        synchronized (documents) {
            documents.remove(uri);
        }
        publishDiagnostics(uri, new ArrayList<>());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params){
        // missing implementation
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params){
        String uri = params.getTextDocument().getUri();
        String content = params.getContentChanges().get(0).getText();

        updateDocument(uri, params.getTextDocument().getVersion(), content);
    }

    public void setClient(LanguageClient client){
        System.err.println("Client connected in Text Document Service");
        this.client = client;
		diagnosticsProvider.setClient( client );
    }

    @JsonRequest("choral/choreographyDiagram")
    public CompletableFuture<Object> choreographyDiagram(Object params) {
        Map<String, Object> request = requestObject(params);
        if (request.isEmpty()) {
            System.err.println("Unsupported choreography request payload: " + (params == null ? "null" : params.getClass().getName()) + " " + params);
        }
        String uri = stringAt(request, "textDocument", "uri");
        if (uri == null) {
            return CompletableFuture.completedFuture(diagramError(
                    "The choreography request did not include a document URI.", "invalidParams"));
        }
        Position position = new Position(
                numberAt(request, "position", "line"),
                numberAt(request, "position", "character"));
        return diagram(uri, position);
    }

    private CompletableFuture<Object> diagram(String uri, Position position) {
        DocumentState state = documents.get(uri);
        if (state != null)
            return diagram(uri, position, state, true);
        String content = readFileDocument(uri);
        if (content == null)
            return CompletableFuture.completedFuture(diagramError(
                    "The document is not open in the Choral language server and could not be read from disk.",
                    "documentUnavailable"));
        DocumentState detached = new DocumentState(null, content, new CompletableFuture<>());
        analyze(uri, detached);
        return diagram(uri, position, detached, false);
    }

    private CompletableFuture<Object> diagram(
            String uri, Position position, DocumentState state, boolean cached
    ) {
        return state.analysis().handle((analysis, failure) -> {
            synchronized (documents) {
                boolean stale = cached
                        ? documents.get(uri) != state
                        : documents.get(uri) != null;
                if (stale)
                    return diagram(uri, position);
                return CompletableFuture.completedFuture(diagram(analysis, failure, position));
            }
        }).thenCompose(result -> result);
    }

    private Object diagram(AnalysisResult analysis, Throwable failure, Position position) {
        if (failure != null)
            return diagramError(
                    "Unable to analyze the Choral document: " + failure.getMessage(),
                    "typeError");
        if (!analysis.successful()) {
            ChoreographyDiagramException.Reason reason =
                    analysis.failure() == AnalysisFailure.PARSE_ERROR
                            ? ChoreographyDiagramException.Reason.PARSE_ERROR
                            : ChoreographyDiagramException.Reason.TYPE_ERROR;
            String action = reason == ChoreographyDiagramException.Reason.PARSE_ERROR
                    ? "parse" : "type-check";
            ChoreographyDiagramException exception = new ChoreographyDiagramException(
                    reason, "Unable to " + action + " the Choral document: "
                            + analysis.failureMessage());
            return diagramError(exception.getMessage(), diagramErrorCode(exception));
        }
        try {
            return choreographyDiagramProvider.diagram(analysis.compilationUnit(), position);
        } catch (ChoreographyDiagramException exception) {
            return diagramError(exception.getMessage(), diagramErrorCode(exception));
        }
    }

    private String readFileDocument(String uri) {
        try {
            if (!uri.startsWith("file:")) return null;
            return Files.readString(Path.of(URI.create(uri)));
        } catch (Exception exception) {
            System.err.println("Unable to read choreography document " + uri + ": " + exception.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> requestObject(Object params) {
        if (params instanceof Map<?, ?> map) return (Map<String, Object>) map;
        if (params instanceof List<?> list && list.size() == 1) return requestObject(list.get(0));
        if (params instanceof JsonArray array && array.size() == 1) return requestObject(array.get(0));
        if (params instanceof JsonObject object) return GSON.fromJson(object, Map.class);
        if (params instanceof JsonElement element && element.isJsonObject()) return GSON.fromJson(element, Map.class);
        return Map.of();
    }

    private static String stringAt(Map<String, Object> params, String objectName, String propertyName) {
        if (!(params.get(objectName) instanceof Map<?, ?> object)) return null;
        Object value = object.get(propertyName);
        return value instanceof String string ? string : null;
    }

    private static int numberAt(Map<String, Object> params, String objectName, String propertyName) {
        if (!(params.get(objectName) instanceof Map<?, ?> object)) return 0;
        Object value = object.get(propertyName);
        return value instanceof Number number ? number.intValue() : 0;
    }

    private void updateDocument(String uri, Integer version, String content) {
        DocumentState proposed = new DocumentState(
                version, content, new CompletableFuture<>());
        DocumentState state;
        synchronized (documents) {
            DocumentState current = documents.get(uri);
            state = current != null && current.matches(version, content) ? current : proposed;
            if (state == proposed)
                documents.put(uri, state);
        }
        state.analysis().thenAccept(analysis -> {
            synchronized (documents) {
                if (documents.get(uri) == state)
                    publishAnalysis(uri, analysis);
            }
        });
        if (state == proposed)
            analyze(uri, state);
    }

    private void analyze(String uri, DocumentState state) {
        try {
            state.analysis().complete(
                    diagnosticsProvider.analyzeTyped(uri, state.content()));
        } catch (Exception failure) {
            state.analysis().completeExceptionally(failure);
        }
    }

    private void publishAnalysis(String uri, AnalysisResult analysis){
        List<Diagnostic> diagnostics = analysis.diagnostics();

        for (Diagnostic d : diagnostics) {
            System.err.println("  - " + d.getMessage() + " at line " + d.getRange().getStart().getLine()
                    + " and at column " + d.getRange().getStart().getCharacter());
        }

        publishDiagnostics(uri, diagnostics);        
    }

    private void publishDiagnostics(String uri, List<Diagnostic> diagnostics){
        System.err.println("=== PUBLISHING DIAGNOSTICS ===");
        System.err.println("URI: " + uri);
        System.err.println("Count: " + diagnostics.size());
        
        if (client == null) {
            System.err.println("ERROR: client is null!");
            return;
        }

        PublishDiagnosticsParams params = new PublishDiagnosticsParams(uri, diagnostics);

        client.publishDiagnostics(params);

        System.err.println("Diagnostics published successfully");
    }

    private static ChoreographyDiagramErrorResult diagramError(String message, String code) {
        return new ChoreographyDiagramErrorResult(message, code);
    }

    private static String diagramErrorCode(ChoreographyDiagramException exception) {
        return switch (exception.reason()) {
            case PARSE_ERROR -> "parseError";
            case TYPE_ERROR -> "typeError";
            case NO_SYMBOL -> "noSymbol";
        };
    }

    public static final class ChoreographyDiagramErrorResult {
        public final ChoreographyDiagramError error;

        private ChoreographyDiagramErrorResult(String message, String code) {
            error = new ChoreographyDiagramError(message, code);
        }
    }

    public record ChoreographyDiagramError(String message, String code) {
    }

    private record DocumentState(
            Integer version, String content, CompletableFuture<AnalysisResult> analysis
    ) {
        private boolean matches(Integer otherVersion, String otherContent) {
            return Objects.equals(version, otherVersion) && content.equals(otherContent);
        }
    }
}
