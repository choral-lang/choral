package lsp;

import choral.ast.CompilationUnit;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.compiler.HeaderLoader;
import choral.compiler.Parser;
import choral.compiler.Typer;
import choral.compiler.moveMeant.MoveMeant;
import choral.compiler.TyperOptions;
import choral.utils.VerbosityLevel;
import com.google.gson.JsonPrimitive;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChoralWorkspaceService implements WorkspaceService {
    private LanguageClient client;

    public void setClient( LanguageClient client ) {
        this.client = client;
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params){
        // missing implementation
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params){
        // missing implementation
    }

    @Override
    public CompletableFuture<Object> executeCommand( ExecuteCommandParams params) {
        System.err.println("=== EXECUTING COMMAND ===");
        System.err.println(params);
        if (params.getCommand().equals("choral.insertComms")) {
            try {
                // Get the file
                var source = ((JsonPrimitive) params.getArguments().get( 0 )).getAsString();

                // Parse it
                CompilationUnit parsedUnit = Parser.parseString( source );
                List<CompilationUnit> headerUnits = HeaderLoader.loadStandardProfile().toList();

                // Collect data dependencies and infer communications
                var opts = new TyperOptions( VerbosityLevel.WARNINGS, this::publishLiftWarning )
                        .relaxedMode();
                var checkedUnit = Typer.annotate( List.of(parsedUnit), headerUnits, opts );
                var fixedUnit = MoveMeant.infer( checkedUnit, headerUnits, opts );

                // Convert to string and overwrite the file
                var fixedSource = new PrettyPrinterVisitor().visit( fixedUnit.get( 0 ) );
                return CompletableFuture.completedFuture(fixedSource);
            }
            catch (Exception e) {
                System.err.println("Error processing file: " + e.getMessage());
            }
            return CompletableFuture.completedFuture(null);
        }
        else {
            System.err.println("Unknown command: " + params.getCommand());
            return CompletableFuture.completedFuture(null);
        }
    }

    private void publishLiftWarning( String message ) {
        if( client != null ) {
            client.logMessage( new MessageParams( MessageType.Warning, message ) );
        }
    }
}
