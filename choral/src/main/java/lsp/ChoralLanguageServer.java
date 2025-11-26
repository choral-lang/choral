package lsp;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class ChoralLanguageServer implements LanguageServer, LanguageClientAware {
    private final ChoralTextDocumentService textDocumentService;
    private final ChoralWorkspaceService workspaceService;
    private LanguageClient client;
    private boolean shutdownReceived = false;
    
    public ChoralLanguageServer() {
        textDocumentService = new ChoralTextDocumentService();
        workspaceService = new ChoralWorkspaceService();
    }

    @Override
    public TextDocumentService getTextDocumentService(){
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService(){
        return workspaceService;
    }

    @Override
    public CompletableFuture<Object> shutdown(){
        // perform cleanup before shutdown.
        shutdownReceived = true;
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit(){
        System.exit(shutdownReceived ? 0 : 1);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params){
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public void connect(LanguageClient client){
        System.err.println("Client connected in Language Server");
        this.client = client;
        textDocumentService.setClient(client);
    }

    public LanguageClient getClient(){
        return client;
    }
}
