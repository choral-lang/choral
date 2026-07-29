import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import choral.ast.CompilationUnit;
import choral.ast.expression.MethodCallExpression;
import choral.ast.expression.ScopedExpression;
import choral.ast.statement.ExpressionStatement;
import choral.compiler.Parser;
import choral.types.GroundDataType;
import lsp.ChoralTextDocumentService;
import lsp.features.DiagnosticsProvider;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChoralTextDocumentServiceTest {
    @Test
    public void typeCheckingAnnotatesTheAstUsedForDiagramExtraction(@TempDir Path project) throws Exception {
        String uri = project.resolve("Example.ch").toUri().toString();
        String source = """
                import choral.channels.SymChannel;

                class Example@( A, B ) {
                    void run( SymChannel@( A, B )< Object > channel, String@A value ) {
                        channel.< String >com( value );
                    }
                }
                """;
        CompilationUnit unit = Parser.parseString(source, project.resolve("Example.ch").toString());

        CompilationUnit typedUnit = new DiagnosticsProvider().typeCheck(uri, unit);

        ExpressionStatement statement = assertInstanceOf(ExpressionStatement.class,
                typedUnit.classes().get(0).methods().get(0).body().orElseThrow());
        ScopedExpression expression = assertInstanceOf(ScopedExpression.class, statement.expression());
        MethodCallExpression call = assertInstanceOf(MethodCallExpression.class,
                expression.scopedExpression());
        assertTrue(call.methodAnnotation().isPresent());
        assertEquals("A", call.methodAnnotation().orElseThrow().signature()
                .parameters().get(0).type().worldArguments().get(0).identifier());
        assertEquals("B", ((GroundDataType) call.methodAnnotation().orElseThrow().returnType())
                .worldArguments().get(0).identifier());
    }

    @Test
    public void returnsMermaidForPositionalCustomRequestParameters(@TempDir Path project) {
        String uri = project.resolve("Example.ch").toUri().toString();
        String source = """
                import choral.channels.SymChannel;

                class Example@( A, B ) {
                    SymChannel@( A, B )< Object > c;
                    void run( String@A value ) { c.< String >com( value ); }
                }
                """;
        ChoralTextDocumentService service = new ChoralTextDocumentService();
        service.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "choral", 1, source)));

        Object result = service.choreographyDiagram(List.of(Map.of(
                "textDocument", Map.of("uri", uri),
                "position", Map.of("line", 4, "character", 45)))).join();

        assertEquals(
                """
                sequenceDiagram
                participant p_A as A
                participant p_B as B
                p_A->>p_B: com
                """.strip(),
                result);
    }

    @Test
    public void reportsTypeCheckingFailures(@TempDir Path project) {
        String uri = project.resolve("Example.ch").toUri().toString();
        String source = """
                class Example@( A, B ) {
                    void run( Missing@A value ) {}
                }
                """;
        ChoralTextDocumentService service = new ChoralTextDocumentService();
        service.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "choral", 1, source)));

        ChoralTextDocumentService.ChoreographyDiagramErrorResult result = assertInstanceOf(
                ChoralTextDocumentService.ChoreographyDiagramErrorResult.class,
                service.choreographyDiagram(Map.of(
                        "textDocument", Map.of("uri", uri),
                        "position", Map.of("line", 1, "character", 10))).join());

        assertEquals("typeError", result.error.code());
    }

    @Test
    public void ownsChoreographyRequestErrorResults() {
        ChoralTextDocumentService service = new ChoralTextDocumentService();

        ChoralTextDocumentService.ChoreographyDiagramErrorResult result = assertInstanceOf(
                ChoralTextDocumentService.ChoreographyDiagramErrorResult.class,
                service.choreographyDiagram(Map.of()).join());

        assertEquals("invalidParams", result.error.code());
    }
}
