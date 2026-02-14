package lsp;

import choral.ast.CompilationUnit;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.compiler.HeaderLoader;
import choral.compiler.Parser;
import choral.compiler.Typer;
import choral.compiler.amend.MoveMeant;
import choral.options.TyperOptions;
import choral.options.VerbosityOptions;
import com.google.gson.JsonPrimitive;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChoralWorkspaceService implements WorkspaceService {
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

                // Collect data dependencies nd infer communications
                var opts = new TyperOptions( VerbosityOptions.VerbosityLevel.WARNINGS );
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
}
