import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import choral.ast.CompilationUnit;
import choral.ast.expression.MethodCallExpression;
import choral.ast.expression.ScopedExpression;
import choral.ast.statement.ExpressionStatement;
import choral.types.GroundDataType;
import lsp.ChoralTextDocumentService;
import lsp.features.DiagnosticsProvider;
import lsp.features.DiagnosticsProvider.AnalysisResult;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        AnalysisResult analysis = new DiagnosticsProvider().analyzeTyped(uri, source);
        assertTrue(analysis.successful());
        CompilationUnit typedUnit = analysis.compilationUnit();

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
                participant p0 as A
                participant p1 as B
                Note over p0,p1: Example.run
                p0->>p1: value
                """.strip(),
                result);
    }

    @Test
    public void rendersOnlyTheMethodAtTheRequestedPosition(@TempDir Path project) {
        String uri = project.resolve("Example.ch").toUri().toString();
        String source = """
                import choral.channels.SymChannel;

                class Example@( A, B ) {
                    SymChannel@( A, B )< Object > forward;
                    SymChannel@( B, A )< Object > reverse;

                    void first( String@A value ) {
                        forward.< String >com( value );
                    }

                    void second( String@B value ) {
                        reverse.< String >com( value );
                    }
                }
                """;
        ChoralTextDocumentService service = new ChoralTextDocumentService();
        service.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "choral", 1, source)));

        assertEquals(
                """
                sequenceDiagram
                participant p0 as A
                participant p1 as B
                Note over p0,p1: Example.first
                p0->>p1: value
                """.strip(),
                diagramAt(service, uri, source, "void first").join());
        assertEquals(
                """
                sequenceDiagram
                participant p0 as A
                participant p1 as B
                Note over p0,p1: Example.second
                p1->>p0: value
                """.strip(),
                diagramAt(service, uri, source, "reverse.< String >com").join());

        ChoralTextDocumentService.ChoreographyDiagramErrorResult result = assertInstanceOf(
                ChoralTextDocumentService.ChoreographyDiagramErrorResult.class,
                diagramAt(service, uri, source, "class Example").join());
        assertEquals("noSymbol", result.error.code());
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

    @Test
    public void reusesTypedAnalysisForUnchangedDiagramRequests(@TempDir Path project) {
        String uri = project.resolve("Example.ch").toUri().toString();
        String source = "class Example@( A, B ) { void run() {} }";
        CountingDiagnosticsProvider diagnostics = new CountingDiagnosticsProvider();
        ChoralTextDocumentService service = new ChoralTextDocumentService(diagnostics);
        service.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "choral", 1, source)));

        assertInstanceOf(String.class, diagramAt(service, uri, source, "run()").join());
        assertInstanceOf(String.class, diagramAt(service, uri, source, "run()").join());

        assertEquals(1, diagnostics.analyses());
    }

    @Test
    public void documentChangesReplaceTheCachedTypedAst(@TempDir Path project) {
        String uri = project.resolve("Example.ch").toUri().toString();
        String first = "class First@( A, B ) { void first() {} }";
        String second = "class Second@( X, Y ) { void second() {} }";
        CountingDiagnosticsProvider diagnostics = new CountingDiagnosticsProvider();
        ChoralTextDocumentService service = new ChoralTextDocumentService(diagnostics);
        service.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "choral", 1, first)));

        service.didChange(new DidChangeTextDocumentParams(
                new VersionedTextDocumentIdentifier(uri, 2),
                List.of(new TextDocumentContentChangeEvent(second))));
        String diagram = assertInstanceOf(
                String.class, diagramAt(service, uri, second, "second()").join());

        assertTrue(diagram.contains("participant p0 as X"));
        assertFalse(diagram.contains("participant p0 as A"));
        assertEquals(2, diagnostics.analyses());
    }

    @Test
    public void closingADocumentInvalidatesItsCachedAnalysis(@TempDir Path project) {
        String uri = project.resolve("Example.ch").toUri().toString();
        String source = "class Example@( A, B ) {}";
        CountingDiagnosticsProvider diagnostics = new CountingDiagnosticsProvider();
        ChoralTextDocumentService service = new ChoralTextDocumentService(diagnostics);
        DidOpenTextDocumentParams open = new DidOpenTextDocumentParams(
                new TextDocumentItem(uri, "choral", 1, source));
        service.didOpen(open);

        service.didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(uri)));
        service.didOpen(open);

        assertEquals(2, diagnostics.analyses());
    }

    @Test
    public void overlappingRequestsWaitForTheCurrentDocumentVersion(@TempDir Path project)
            throws Exception {
        String uri = project.resolve("Example.ch").toUri().toString();
        String first = "class First@( A, B ) { void first() {} }";
        String second = "class Second@( X, Y ) { void second() {} }";
        BlockingDiagnosticsProvider diagnostics = new BlockingDiagnosticsProvider("Second");
        ChoralTextDocumentService service = new ChoralTextDocumentService(diagnostics);
        service.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "choral", 1, first)));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            CompletableFuture<Void> change = CompletableFuture.runAsync(() ->
                    service.didChange(new DidChangeTextDocumentParams(
                            new VersionedTextDocumentIdentifier(uri, 2),
                            List.of(new TextDocumentContentChangeEvent(second)))), executor);
            assertTrue(diagnostics.awaitAnalysis());

            CompletableFuture<Object> firstRequest = diagramAt(service, uri, second, "second()");
            CompletableFuture<Object> secondRequest = diagramAt(service, uri, second, "second()");
            assertFalse(firstRequest.isDone());
            assertFalse(secondRequest.isDone());

            diagnostics.releaseAnalysis();
            change.get(10, TimeUnit.SECONDS);
            String firstDiagram = assertInstanceOf(
                    String.class, firstRequest.get(10, TimeUnit.SECONDS));
            String secondDiagram = assertInstanceOf(
                    String.class, secondRequest.get(10, TimeUnit.SECONDS));
            assertTrue(firstDiagram.contains("participant p0 as X"));
            assertEquals(firstDiagram, secondDiagram);
            assertEquals(2, diagnostics.analyses());
        } finally {
            diagnostics.releaseAnalysis();
            executor.shutdownNow();
        }
    }

    @Test
    public void cachedAnalysisResolvesProjectHeaders(@TempDir Path project) throws Exception {
        Files.writeString(project.resolve("ProjectValue.chh"), """
                interface ProjectValue@( X ) {
                    String@X identity( String@X value );
                }
                """);
        String uri = project.resolve("Example.ch").toUri().toString();
        String source = """
                import choral.channels.SymChannel;

                class Example@( A, B ) {
                    void run(
                            SymChannel@( A, B )< Object > channel,
                            ProjectValue@A helper,
                            String@A value ) {
                        String@A local = helper.identity( value );
                        channel.< String >com( local );
                    }
                }
                """;
        CountingDiagnosticsProvider diagnostics = new CountingDiagnosticsProvider();
        ChoralTextDocumentService service = new ChoralTextDocumentService(diagnostics);
        service.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "choral", 1, source)));

        String diagram = assertInstanceOf(
                String.class, diagramAt(service, uri, source, "channel.< String >com").join());

        assertTrue(diagram.contains("p0->>p1: local"));
        assertEquals(1, diagnostics.analyses());
    }

    @Test
    public void expandsImportedSourceMethodsFromTheWorkspace(@TempDir Path project)
            throws Exception {
        Files.writeString(project.resolve("Helper.ch"), """
                package helpers;

                import choral.channels.SymChannel;

                public class Helper@( Sender, Receiver ) {
                    public void send(
                            SymChannel@( Sender, Receiver )< Object > channel,
                            String@Sender value ) {
                        channel.< String >com( value );
                    }
                }
                """);
        Files.writeString(project.resolve("Helper.chh"), """
                package helpers;

                public interface Helper@( Sender, Receiver ) {}
                """);
        String uri = project.resolve("Root.ch").toUri().toString();
        String source = """
                package app;

                import choral.channels.SymChannel;
                import helpers.Helper;

                class Root@( A, B ) {
                    void run(
                            Helper@( B, A ) helper,
                            SymChannel@( B, A )< Object > channel,
                            String@B value ) {
                        helper.send( channel, value );
                    }
                }
                """;
        ChoralTextDocumentService service = new ChoralTextDocumentService();
        service.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "choral", 1, source)));

        assertEquals(
                """
                sequenceDiagram
                participant p0 as A
                participant p1 as B
                Note over p0,p1: Root.run
                rect rgba(0, 0, 0, 0.05)
                Note left of p0: call Helper.send
                p1->>p0: value
                end
                """.strip(),
                diagramAt(service, uri, source, "helper.send").join());
    }

    private static CompletableFuture<Object> diagramAt(
            ChoralTextDocumentService service, String uri, String source, String marker
    ) {
        int offset = source.indexOf(marker);
        int line = 0;
        int character = 0;
        for (int index = 0; index < offset; index++) {
            if (source.charAt(index) == '\n') {
                line++;
                character = 0;
            } else {
                character++;
            }
        }
        return service.choreographyDiagram(Map.of(
                "textDocument", Map.of("uri", uri),
                "position", Map.of("line", line, "character", character)));
    }

    private static class CountingDiagnosticsProvider extends DiagnosticsProvider {
        private final AtomicInteger analyses = new AtomicInteger();

        @Override
        public AnalysisResult analyzeTyped(String uri, String content) {
            analyses.incrementAndGet();
            return super.analyzeTyped(uri, content);
        }

        protected int analyses() {
            return analyses.get();
        }
    }

    private static final class BlockingDiagnosticsProvider extends CountingDiagnosticsProvider {
        private final String marker;
        private final CountDownLatch analysisStarted = new CountDownLatch(1);
        private final CountDownLatch continueAnalysis = new CountDownLatch(1);

        private BlockingDiagnosticsProvider(String marker) {
            this.marker = marker;
        }

        @Override
        public AnalysisResult analyzeTyped(String uri, String content) {
            if (content.contains(marker)) {
                analysisStarted.countDown();
                try {
                    if (!continueAnalysis.await(10, TimeUnit.SECONDS))
                        throw new IllegalStateException("Timed out waiting to continue analysis");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(exception);
                }
            }
            return super.analyzeTyped(uri, content);
        }

        private boolean awaitAnalysis() throws InterruptedException {
            return analysisStarted.await(10, TimeUnit.SECONDS);
        }

        private void releaseAnalysis() {
            continueAnalysis.countDown();
        }
    }
}
