import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import choral.diagrams.ChoreographyDiagram;
import lsp.ChoralTextDocumentService;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ChoralTextDocumentServiceTest {
    @Test
    public void acceptsPositionalCustomRequestParameters() {
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
                "textDocument", Map.of("uri", uri),
                "position", Map.of("line", 2, "character", 45)))).join();

        assertInstanceOf(ChoreographyDiagram.class, result);
        JsonObject json = new Gson().toJsonTree(result).getAsJsonObject();
        assertEquals(1, json.get("version").getAsInt());
        assertEquals("Example", json.getAsJsonObject("symbol").get("name").getAsString());
        assertEquals(2, json.getAsJsonArray("participants").size());
        assertEquals("message", json.getAsJsonArray("events").get(0).getAsJsonObject().get("kind").getAsString());
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
