import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import choral.diagrams.ChoreographyDiagram;
import choral.diagrams.ChoreographyDiagram.Branch;
import choral.diagrams.ChoreographyDiagram.Group;
import choral.diagrams.ChoreographyDiagram.Message;
import choral.diagrams.ChoreographyDiagram.Participant;
import choral.diagrams.ChoreographyDiagram.Position;
import choral.diagrams.ChoreographyDiagram.Range;
import choral.diagrams.MermaidDiagramPrinter;

public class MermaidDiagramPrinterTest {
	private static final Range RANGE = new Range(
			new Position( 0, 0 ), new Position( 0, 1 ) );

	@Test
	public void participantsMessagesAndSelections() {
		ChoreographyDiagram diagram = new ChoreographyDiagram();
		diagram.participants.add( new Participant( "Buyer-1", "Buyer: {one}" ) );
		diagram.participants.add( new Participant( "Seller", "Seller\nTeam" ) );
		diagram.events.add( message(
				"message", "Buyer-1", "Seller", "book: {title};" ) );
		diagram.events.add( message(
				"selection", "Seller", "Buyer-1", "YES" ) );

		MermaidDiagramPrinter printer = new MermaidDiagramPrinter();
		assertEquals( "mermaid", printer.format() );
		assertEquals(
				"""
				sequenceDiagram
				participant p_Buyer_1 as Buyer   one
				participant p_Seller as Seller Team
				p_Buyer_1->>p_Seller: book   title
				p_Seller-->>p_Buyer_1: YES""",
				printer.print( diagram ) );
	}

	@Test
	public void nestedGroupsAndEmptyAlternative() {
		ChoreographyDiagram diagram = new ChoreographyDiagram();
		diagram.participants.add( new Participant( "A", "A" ) );
		diagram.participants.add( new Participant( "B", "B" ) );
		diagram.events.add( new Group(
				"alt",
				"unused",
				List.of(
						new Branch(
								"success: yes",
								List.of( message( "message", "A", "B", "ok" ) ) ),
						new Branch(
								"failure",
								List.of( message( "selection", "B", "A", "retry" ) ) ) ),
				null,
				RANGE ) );
		diagram.events.add( new Group(
				"opt",
				"send; receipt",
				null,
				List.of( message( "message", "B", "A", "receipt" ) ),
				RANGE ) );
		diagram.events.add( new Group(
				"loop",
				"while {open}",
				null,
				List.of( message( "selection", "A", "B", "again" ) ),
				RANGE ) );
		diagram.events.add( new Group(
				"alt", "fallback", List.of(), null, RANGE ) );

		assertEquals(
				"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				alt success  yes
					p_A->>p_B: ok
				else failure
					p_B-->>p_A: retry
				end
				opt send  receipt
					p_B->>p_A: receipt
				end
				loop while  open
					p_A-->>p_B: again
				end
				alt fallback
				end""",
				new MermaidDiagramPrinter().print( diagram ) );
	}

	@Test
	public void emptyEscapedLabelRemainsRenderable() {
		ChoreographyDiagram diagram = new ChoreographyDiagram();
		diagram.participants.add( new Participant( "empty", ":{};" ) );

		assertEquals(
				"sequenceDiagram\nparticipant p_empty as  ",
				new MermaidDiagramPrinter().print( diagram ) );
	}

	private static Message message( String kind, String from, String to, String label ) {
		return new Message( kind, from, to, label, RANGE );
	}
}
