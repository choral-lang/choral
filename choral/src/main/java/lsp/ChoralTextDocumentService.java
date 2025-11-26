package lsp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import lsp.features.DiagnosticsProvider;

public class ChoralTextDocumentService implements TextDocumentService {
    private final DiagnosticsProvider diagnosticsProvider;
    private LanguageClient client;
    
    public ChoralTextDocumentService() {
        diagnosticsProvider = new DiagnosticsProvider();
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params){
        String uri = params.getTextDocument().getUri();
        String content = params.getTextDocument().getText();


        System.err.println("=== DID OPEN ===");
        System.err.println("URI: " + uri);
        System.err.println("Content length: " + content.length());
        System.err.println("First 100 chars: " + content.substring(0, Math.min(100, content.length())));

        analyzeAndPublish(uri, content);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params){
        String uri = params.getTextDocument().getUri();
        publishDiagnostics(uri, new ArrayList<>());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params){
        String uri = params.getTextDocument().getUri();
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params){
        String uri = params.getTextDocument().getUri();
        String content = params.getContentChanges().get(0).getText();

        analyzeAndPublish(uri, content);
    }

    public void setClient(LanguageClient client){
        System.err.println("Client connected in Text Document Service");
        this.client = client;
    }

    private void analyzeAndPublish(String uri, String content){
        List<Diagnostic> diagnostics = diagnosticsProvider.analyze(uri, content);

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
}
