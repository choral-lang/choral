import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import lsp.ChoralLanguageServer;
import lsp.ChoralTextDocumentService;
import lsp.ChoralTextDocumentService.RenderedChoreographyDiagram;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ChoralTextDocumentServiceTest {
    @Test
    public void returnsRenderedMermaidForPositionalCustomRequestParameters() {
        String uri = "file:///Example.ch";
        String source = """
                class Example@( A, B ) {
                    SymChannel@( A, B )< Object > c;
                    void run( String@A value ) { c.< String >com( value ); }
                }
                """;
        ChoralTextDocumentService service = new ChoralTextDocumentService();
        service.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "choral", 1, source)));

        Object result = service.choreographyDiagram(List.of(Map.of(
                "format", "mermaid",
                "textDocument", Map.of("uri", uri),
                "position", Map.of("line", 2, "character", 45)))).join();

        assertInstanceOf(RenderedChoreographyDiagram.class, result);
        JsonObject json = new Gson().toJsonTree(result).getAsJsonObject();
        assertEquals(2, json.get("version").getAsInt());
        assertEquals("mermaid", json.get("format").getAsString());
        assertEquals(
                """
                sequenceDiagram
                participant p_A as A
                participant p_B as B
                p_A->>p_B: com
                """.strip(),
                json.get("source").getAsString());
    }

    @Test
    public void rejectsUnsupportedDiagramFormats() {
        ChoralTextDocumentService service = new ChoralTextDocumentService();

        ChoralTextDocumentService.ChoreographyDiagramErrorResult result = assertInstanceOf(
                ChoralTextDocumentService.ChoreographyDiagramErrorResult.class,
                service.choreographyDiagram(Map.of("format", "plantuml")).join());

        assertEquals("unsupportedFormat", result.error.code());
    }

    @Test
    public void rejectsNonStringDiagramFormats() {
        ChoralTextDocumentService service = new ChoralTextDocumentService();

        ChoralTextDocumentService.ChoreographyDiagramErrorResult result = assertInstanceOf(
                ChoralTextDocumentService.ChoreographyDiagramErrorResult.class,
                service.choreographyDiagram(Map.of("format", 2)).join());

        assertEquals("invalidParams", result.error.code());
    }

    @Test
    public void advertisesRenderedDiagramCapability() {
        Object experimental = new ChoralLanguageServer().initialize(new InitializeParams()).join()
                .getCapabilities().getExperimental();
        Map<?, ?> experimentalMap = assertInstanceOf(Map.class, experimental);
        Map<?, ?> choral = assertInstanceOf(Map.class, experimentalMap.get("choral"));
        Map<?, ?> capability = assertInstanceOf(Map.class, choral.get("choreographyDiagram"));

        assertEquals(2, capability.get("version"));
        assertEquals(List.of("mermaid"), capability.get("formats"));
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
