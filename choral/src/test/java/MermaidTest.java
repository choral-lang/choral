import choral.diagrams.ChoreographyDiagramProvider;
import choral.diagrams.ChoreographyDiagramProvider.Position;
import choral.compiler.HeaderLoader;
import choral.compiler.Parser;
import choral.compiler.Typer;
import choral.compiler.TyperOptions;
import choral.utils.VerbosityLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

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
	public void nestedStatementsAndExpressionsAreTraversed() {
		String source =
			"""
			import choral.channels.SymChannel;

			enum Choice@X { THEN, ELSE }

			class Nested@( A, B ) {
				void consume( String@B value ) {}

				void run(
						SymChannel@( A, B )< Object > channel,
						String@A value ) {
					if( true@A ) {
						channel.< Choice >select( Choice@A.THEN );
						consume( channel.< String >com( value ) );
					} else {
						channel.< Choice >select( Choice@A.ELSE );
						channel.< String >com( value );
					}
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				alt true@A
				p_A-->>p_B: select
				p_A->>p_B: com
				else
				p_A-->>p_B: select
				p_A->>p_B: com
				end
				""".strip(),
				mermaidAt( source, "consume( channel" ) );
	}

	@Test
	public void nestedConditionalsPreserveStructureAndOrder() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Alternatives@( A, B, C ) {
				void run(
						SymChannel@( A, B )< Object > ab,
						SymChannel@( B, C )< Object > bc,
						SymChannel@( C, A )< Object > ca,
						String@A a,
						String@B b,
						String@C c ) {
					if( true@A ) {
						ab.< String >com( a );
						if( false@B ) {
							bc.< String >com( b );
						} else {
							ca.< String >com( c );
						}
					} else {
						ab.< String >com( a );
					}
					bc.< String >com( b );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				participant p_C as C
				alt true@A
				p_A->>p_B: com
				alt false@B
				p_B->>p_C: com
				else
				p_C->>p_A: com
				end
				else
				p_A->>p_B: com
				end
				p_B->>p_C: com
				""".strip(),
				mermaidAt( source, "if( false@B )" ) );
	}

	@Test
	public void switchCasesAreRenderedAsOrderedAlternatives() {
		String source =
			"""
			import choral.channels.SymChannel;

			enum Route@X { FAST, SAFE }

			class Routing@( A, B, C ) {
				void run(
						SymChannel@( A, B )< Object > ab,
						SymChannel@( B, C )< Object > bc,
						SymChannel@( C, A )< Object > ca,
						Route@A route,
						String@A a,
						String@B b,
						String@C c ) {
					switch( route ) {
						case FAST -> { ab.< String >com( a ); }
						case SAFE -> { bc.< String >com( b ); }
						default -> { ca.< String >com( c ); }
					}
					ab.< String >com( a );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				participant p_C as C
				alt route = FAST
				p_A->>p_B: com
				else route = SAFE
				p_B->>p_C: com
				else default
				p_C->>p_A: com
				end
				p_A->>p_B: com
				""".strip(),
				mermaidAt( source, "case SAFE" ) );
	}

	@Test
	public void tryCatchPathsUseCriticalOptionsInOrder() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Recovery@( A, B, C ) {
				void run(
						SymChannel@( A, B )< Object > ab,
						SymChannel@( B, C )< Object > bc,
						SymChannel@( C, A )< Object > ca,
						String@A a,
						String@B b,
						String@C c ) {
					try {
						ab.< String >com( a );
					} catch( Exception@B first ) {
						bc.< String >com( b );
					} catch( Exception@C second ) {
						ca.< String >com( c );
					}
					bc.< String >com( b );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				participant p_C as C
				critical try
				p_A->>p_B: com
				option catch Exception@( B ) first
				p_B->>p_C: com
				option catch Exception@( C ) second
				p_C->>p_A: com
				end
				p_B->>p_C: com
				""".strip(),
				mermaidAt( source, "Exception@C second" ) );
	}

	@Test
	public void fieldAndParameterChannelsAcrossMethodsAreTraversedInOrder() {
		String source =
			"""
			import choral.channels.SymChannel;

			class ChannelSources@( A, B ) {
				SymChannel@( A, B )< Object > field;

				void fromField( String@A value ) {
					field.< String >com( value );
				}

				void fromParameter(
						SymChannel@( B, A )< Object > parameter,
						String@B value ) {
					parameter.< String >com( value );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				p_A->>p_B: com
				p_B->>p_A: com
				""".strip(),
				mermaidAt( source, "parameter.< String >com" ) );
	}

	@Test
	public void onlyTypedChannelCallsBecomeEvents() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Helper@( X ) {
				String@X com( String@X value ) { return value; }
				String@X select( String@X value ) { return value; }
			}

			class Typed@( A, B ) {
				void run(
						SymChannel@( A, B )< Object > arbitrarilyNamed,
						Helper@A helper,
						String@A value ) {
					// fake.< String >com( value );
					String@A fakeText = "arbitrarilyNamed.< String >com( value )"@A;
					String@A local = helper.com( value );
					String@A selected = helper.select( value );
					String@B received = arbitrarilyNamed.< String >com(
							value
					);
				}
			}
			""";

		assertEquals(
				"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				p_A->>p_B: com
				""".strip(),
				mermaidAt( source, "arbitrarilyNamed.< String >com" ) );
	}

	@Test
	public void constructorCommunicationsAreExcluded() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Constructed@( A, B ) {
				public Constructed(
						SymChannel@( A, B )< Object > channel,
						String@A value ) {
					channel.< String >com( value );
				}

				void run() {}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				""".strip(),
				mermaidAt( source, "void run" ) );
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
		try {
			var unit = Parser.parseString( source );
			Typer.annotate(
					List.of( unit ), HeaderLoader.loadStandardProfile().toList(),
					new TyperOptions( VerbosityLevel.WARNINGS ) );
			return new ChoreographyDiagramProvider().diagram(
					unit, new Position( line, character ) );
		} catch( Exception exception ) {
			throw new RuntimeException( exception );
		}
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
