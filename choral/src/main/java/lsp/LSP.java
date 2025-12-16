package lsp;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;

public class LSP {
    public static void main(String[] args, boolean debug) {
        ChoralLanguageServer server = new ChoralLanguageServer();
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
        try {
            System.err.println("getting proxy");
            LanguageClient client = launcher.getRemoteProxy();

            System.err.println("connecting client to server");
            if (server instanceof LanguageClientAware) {
                ((LanguageClientAware) server).connect(client);
            }

            System.err.println("Starting listening");
            launcher.startListening().get();  
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
