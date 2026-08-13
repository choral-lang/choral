package lsp;

import choral.diagrams.ChoreographyDiagramProvider;
import choral.diagrams.ChoreographyDiagramProvider.Position;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
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
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import lsp.features.DiagnosticsProvider;
import lsp.features.TypedSourceAnalyzer;
import lsp.features.TypedSourceAnalyzer.AnalysisFailure;
import lsp.features.TypedSourceAnalyzer.AnalysisResult;

public class ChoralTextDocumentService implements TextDocumentService {
    private final TypedSourceAnalyzer analyzer;
    private final DiagnosticsProvider diagnosticsProvider;
    private final ChoreographyDiagramProvider choreographyDiagramProvider;
    private final Map<String, String> documents = new ConcurrentHashMap<>();
    private LanguageClient client;
    
    public ChoralTextDocumentService() {
        this(new TypedSourceAnalyzer());
    }

    public ChoralTextDocumentService(TypedSourceAnalyzer analyzer) {
        this.analyzer = Objects.requireNonNull(analyzer);
        diagnosticsProvider = new DiagnosticsProvider();
        choreographyDiagramProvider = new ChoreographyDiagramProvider();
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params){
        String uri = params.getTextDocument().getUri();
        String content = params.getTextDocument().getText();

        updateDocument(uri, content);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params){
        String uri = params.getTextDocument().getUri();
        documents.remove(uri);
        publishDiagnostics(uri, List.of());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params){
        // missing implementation
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params){
        String uri = params.getTextDocument().getUri();
        String content = params.getContentChanges().get(0).getText();

        updateDocument(uri, content);
    }

    public void setClient(LanguageClient client){
        System.err.println("Client connected in Text Document Service");
        this.client = client;
		diagnosticsProvider.setClient( client );
    }

    @JsonRequest("choral/choreographyDiagram")
    public CompletableFuture<String> choreographyDiagram(TextDocumentPositionParams params) {
        if (params == null || params.getTextDocument() == null
                || params.getTextDocument().getUri() == null) {
            return CompletableFuture.failedFuture(diagramFailure(
                    "The choreography request did not include a document URI.",
                    ResponseErrorCode.InvalidParams));
        }
        String uri = params.getTextDocument().getUri();
        if (params.getPosition() == null) {
            return CompletableFuture.failedFuture(diagramFailure(
                    "The choreography request did not include a cursor position.",
                    ResponseErrorCode.InvalidParams));
        }
        Position position = new Position(
                params.getPosition().getLine(), params.getPosition().getCharacter());
        return diagram(uri, position);
    }

    private CompletableFuture<String> diagram(String uri, Position position) {
        String content = documents.get(uri);
        if (content == null) content = readFileDocument(uri);
        if (content == null)
            return CompletableFuture.failedFuture(diagramFailure(
                    "The document is not open in the Choral language server and could not be read from disk.",
                    ResponseErrorCode.RequestFailed));
        try {
            AnalysisResult analysis = analyzer.analyze(uri, content, openDocuments());
            return CompletableFuture.completedFuture(diagram(analysis, position));
        } catch (ResponseErrorException failure) {
            return CompletableFuture.failedFuture(failure);
        } catch (Exception failure) {
            return CompletableFuture.failedFuture(diagramFailure(
                    "Unable to analyze the Choral document: " + failure.getMessage(),
                    ResponseErrorCode.InternalError));
        }
    }

    private String diagram(AnalysisResult analysis, Position position) {
        if (!analysis.successful()) {
            boolean parseError = analysis.failure() == AnalysisFailure.PARSE_ERROR;
            String action = parseError ? "parse" : "type-check";
            throw diagramFailure("Unable to " + action + " the Choral document: "
                    + analysis.failureMessage(), ResponseErrorCode.RequestFailed);
        }
        return choreographyDiagramProvider.diagram(analysis.compilationUnit(), position)
                .orElseThrow(() -> diagramFailure(
                        "No choreography method was found at the cursor.",
                        ResponseErrorCode.RequestFailed));
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

    private void updateDocument(String uri, String content) {
        documents.put(uri, content);
        try {
            AnalysisResult analysis = analyzer.analyze(uri, content, openDocuments());
            if (content.equals(documents.get(uri)))
                publishAnalysis(uri, analysis);
        } catch (Exception failure) {
            System.err.println("Unable to analyze Choral document " + uri + ": "
                    + failure.getMessage());
        }
    }

    private Map<String, String> openDocuments() {
        Map<String, String> contents = new LinkedHashMap<>();
        contents.putAll(documents);
        return contents;
    }

    private void publishAnalysis(String uri, AnalysisResult analysis){
        List<Diagnostic> diagnostics = diagnosticsProvider.diagnostics(uri, analysis);

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

    private static ResponseErrorException diagramFailure(
            String message, ResponseErrorCode code
    ) {
        return new ResponseErrorException(new ResponseError(code, message, null));
    }
}
