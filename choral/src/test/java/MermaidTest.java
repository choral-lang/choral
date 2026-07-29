import choral.diagrams.ChoreographyDiagram;
import choral.diagrams.ChoreographyDiagram.Position;
import choral.diagrams.ChoreographyDiagramProvider;
import choral.diagrams.MermaidDiagramPrinter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MermaidTest {
	@Test
	public void emptyChoreography() {
		String source =
			"""
			class Empty@( Customer, Seller ) {
			}
			""";

		assertEquals(
				"""
				sequenceDiagram
				participant p_Customer as Customer
				participant p_Seller as Seller
				""".strip(),
				mermaid( source, 0, 0 ) );
	}

	@Test
	public void oneCommunication() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Order@( Customer, Seller ) {
				public void run(
						SymChannel@( Customer, Seller )< Object > channel,
						String@Customer order ) {
					String@Seller received = channel.< String >com( order );
				}
			}
			""";

		assertEquals(
				"""
				sequenceDiagram
				participant p_Customer as Customer
				participant p_Seller as Seller
				p_Customer->>p_Seller: com
				""".strip(),
				mermaid( source, 6, 10 ) );
	}

	@Test
	public void oneSelection() {
		String source =
			"""
			import choral.channels.SymChannel;

			enum Decision@X { ACCEPT }

			class Approval@( Customer, Seller ) {
				public void run( SymChannel@( Customer, Seller )< Object > channel ) {
					channel.< Decision >select( Decision@Customer.ACCEPT );
				}
			}
			""";

		assertEquals(
				"""
				sequenceDiagram
				participant p_Customer as Customer
				participant p_Seller as Seller
				p_Customer-->>p_Seller: select
				""".strip(),
				mermaid( source, 6, 10 ) );
	}

	@Test
	public void multipleEventsPreserveSourceOrder() {
		String source =
			"""
			import choral.channels.SymChannel;

			enum State@X { DONE }

			class Shipping@( Customer, Seller, Shipper ) {
				public void run(
						SymChannel@( Customer, Seller )< Object > customerSeller,
						SymChannel@( Seller, Shipper )< Object > sellerShipper,
						String@Customer order ) {
					String@Seller received = customerSeller.< String >com( order );
					sellerShipper.< String >com( received );
					sellerShipper.< State >select( State@Shipper.DONE );
				}
			}
			""";

		assertEquals(
				"""
				sequenceDiagram
				participant p_Customer as Customer
				participant p_Seller as Seller
				participant p_Shipper as Shipper
				p_Customer->>p_Seller: com
				p_Seller->>p_Shipper: com
				p_Shipper-->>p_Seller: select
				""".strip(),
				mermaid( source, 10, 10 ) );
	}

	@Test
	public void participantIdentifiersAreSanitized() {
		String source =
			"""
			class International@( Customer$EU, Seller ) {
			}
			""";

		assertEquals(
				"""
				sequenceDiagram
				participant p_Customer_EU as Customer$EU
				participant p_Seller as Seller
				""".strip(),
				mermaid( source, 0, 0 ) );
	}

	@Test
	public void selectsTheCorrectChoreography() {
		String source =
			"""
			import choral.channels.SymChannel;

			enum Decision@W { ACCEPT }

			class First@( A, B ) {
				public void run(
						SymChannel@( A, B )< Object > firstChannel,
						String@A value ) {
					firstChannel.< String >com( value );
				}
			}

			class Second@( X, Y ) {
				public void run( SymChannel@( X, Y )< Object > secondChannel ) {
					secondChannel.< Decision >select( Decision@Y.ACCEPT );
				}
			}
			""";

		assertEquals(
				"""
				sequenceDiagram
				participant p_X as X
				participant p_Y as Y
				p_Y-->>p_X: select
				""".strip(),
				mermaidAt( source, "secondChannel.< Decision >select" ) );
	}

	private static String mermaid( String source, int line, int character ) {
		ChoreographyDiagram diagram = new ChoreographyDiagramProvider().diagram(
				source, new Position( line, character ) );
		return new MermaidDiagramPrinter().print( diagram );
	}

	private static String mermaidAt( String source, String marker ) {
		int offset = source.indexOf( marker );
		if( offset < 0 )
			throw new IllegalArgumentException( "Marker not found: " + marker );
		int line = 0;
		int character = 0;
		for( int index = 0; index < offset; index++ ) {
			if( source.charAt( index ) == '\n' ) {
				line++;
				character = 0;
			} else {
				character++;
			}
		}
		return mermaid( source, line, character );
	}
}
