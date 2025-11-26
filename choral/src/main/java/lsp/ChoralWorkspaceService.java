package lsp;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

public class ChoralWorkspaceService implements WorkspaceService {
    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params){
        // missing implementation
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params){
        // missing implementation
    }
}
