package lsp;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

public class LSP {
    public static void main(String[] args, boolean debug) {
        boolean temp = debug;
        //Boolean.parseBoolean(args[0]);
        ChoralLanguageServer server = new ChoralLanguageServer();
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
        try {
            launcher.startListening().get();  
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
