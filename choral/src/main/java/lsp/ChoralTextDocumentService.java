package lsp;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import choral.diagrams.ChoreographyDiagram;
import choral.diagrams.ChoreographyDiagramException;
import choral.diagrams.ChoreographyDiagramProvider;
import choral.diagrams.ChoreographyDiagramPrinter;
import choral.diagrams.MermaidDiagramPrinter;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

public class ChoralTextDocumentService implements TextDocumentService {
    private static final Gson GSON = new Gson();
    private final DiagnosticsProvider diagnosticsProvider;
    private final ChoreographyDiagramProvider choreographyDiagramProvider;
    private final ChoreographyDiagramPrinter choreographyDiagramPrinter;
    private final Map<String, String> documents = new ConcurrentHashMap<>();
    private LanguageClient client;
    
    public ChoralTextDocumentService() {
        diagnosticsProvider = new DiagnosticsProvider();
        choreographyDiagramProvider = new ChoreographyDiagramProvider();
        choreographyDiagramPrinter = new MermaidDiagramPrinter();
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params){
        String uri = params.getTextDocument().getUri();
        String content = params.getTextDocument().getText();
        
        documents.put(uri, content);
        analyzeAndPublish(uri, content);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params){
        String uri = params.getTextDocument().getUri();
        documents.remove(uri);
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

        documents.put(uri, content);
        analyzeAndPublish(uri, content);
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
        String content = documents.get(uri);
        if (content == null) {
            content = readFileDocument(uri);
        }
        if (content == null) {
            return CompletableFuture.completedFuture(diagramError(
                    "The document is not open in the Choral language server and could not be read from disk.",
                    "documentUnavailable"));
        }
        ChoreographyDiagram.Position position = new ChoreographyDiagram.Position(
                numberAt(request, "position", "line"),
                numberAt(request, "position", "character"));
        try {
            ChoreographyDiagram diagram = choreographyDiagramProvider.diagram(content, position);
            return CompletableFuture.completedFuture(choreographyDiagramPrinter.print(diagram));
        } catch (ChoreographyDiagramException exception) {
            return CompletableFuture.completedFuture(diagramError(
                    exception.getMessage(), diagramErrorCode(exception)));
        }
    }

    private String readFileDocument(String uri) {
        try {
            if (!uri.startsWith("file:")) return null;
            String content = Files.readString(Path.of(URI.create(uri)));
            documents.put(uri, content);
            return content;
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

    private void analyzeAndPublish(String uri, String content){
        List<Diagnostic> diagnostics = diagnosticsProvider.analyze(uri, content);

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
}
