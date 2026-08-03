import choral.diagrams.ChoreographyDiagramProvider;
import choral.diagrams.ChoreographyDiagramProvider.Position;
import choral.diagrams.ChoreographyDiagramException;
import choral.diagrams.MermaidVisitor;
import choral.compiler.HeaderLoader;
import choral.compiler.Parser;
import choral.compiler.Typer;
import choral.compiler.TyperOptions;
import choral.utils.VerbosityLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MermaidTest {
	@Test
	public void emptyMethod() {
		String source =
			"""
			class Empty@( Customer, Seller ) {
				void run() {}
			}
			""";

		assertEquals(
				"""
				sequenceDiagram
				participant p_Customer as Customer
				participant p_Seller as Seller
				Note over p_Customer,p_Seller: Empty.run
				""".strip(),
				mermaidAt( source, "void run" ) );
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
				Note over p_Customer,p_Seller: Order.run
				p_Customer->>p_Seller: order
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
				Note over p_Customer,p_Seller: Approval.run
				p_Customer-->>p_Seller: Decision@Customer.ACCEPT
				""".strip(),
				mermaid( source, 6, 10 ) );
	}

	@Test
	public void eventLabelsEscapeMermaidSyntax() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Escaped@( A, B ) {
				void run( SymChannel@( A, B )< Object > channel ) {
					channel.< String >com( "ready: <go>; %% end"@A );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: Escaped.run
				p_A->>p_B: "ready go % end"@A
				""".strip(),
				mermaidAt( source, "ready:" ) );
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
				Note over p_Customer,p_Shipper: Shipping.run
				p_Customer->>p_Seller: order
				p_Seller->>p_Shipper: received
				p_Shipper-->>p_Seller: State@Shipper.DONE
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
				Note over p_A,p_B: Nested.run
				alt true@A
				p_A-->>p_B: Choice@A.THEN
				p_A->>p_B: value
				else
				p_A-->>p_B: Choice@A.ELSE
				p_A->>p_B: value
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
				Note over p_A,p_C: Alternatives.run
				alt true@A
				p_A->>p_B: a
				alt false@B
				p_B->>p_C: b
				else
				p_C->>p_A: c
				end
				else
				p_A->>p_B: a
				end
				p_B->>p_C: b
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
				Note over p_A,p_C: Routing.run
				alt route = FAST
				p_A->>p_B: a
				else route = SAFE
				p_B->>p_C: b
				else default
				p_C->>p_A: c
				end
				p_A->>p_B: a
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
				Note over p_A,p_C: Recovery.run
				critical try
				p_A->>p_B: a
				option catch Exception@( B ) first
				p_B->>p_C: b
				option catch Exception@( C ) second
				p_C->>p_A: c
				end
				p_B->>p_C: b
				""".strip(),
				mermaidAt( source, "Exception@C second" ) );
	}

	@Test
	public void controlFlowWithoutDiagramContentIsOmitted() {
		String source =
			"""
			import choral.channels.SymChannel;

			enum Choice@X { FIRST }

			class EmptyControlFlow@( A, B ) {
				void run(
						SymChannel@( A, B )< Object > channel,
						Boolean@A condition,
						Choice@A choice,
						String@A value ) {
					if( condition ) {}
					switch( choice ) {
						case FIRST -> {}
						default -> {}
					}
					try {} catch( Exception@A error ) {}
					channel.< String >com( value );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: EmptyControlFlow.run
				p_A->>p_B: value
				""".strip(),
				mermaidAt( source, "void run" ) );
	}

	@Test
	public void emptyNestedControlFlowIsOmittedFromVisibleAlternatives() {
		String source =
			"""
			import choral.channels.SymChannel;

			enum StreamState@X { OFF }

			class Streaming@( A, B ) {
				void gather(
						SymChannel@( A, B )< Object > channel,
						Boolean@A streaming,
						Boolean@B valid,
						String@A value ) {
					if( streaming ) {
						channel.< String >com( value );
						if( valid ) {}
						gather( channel, streaming, valid, value );
					} else {
						channel.< StreamState >select( StreamState@A.OFF );
					}
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: Streaming.gather
				alt streaming
				p_A->>p_B: value
				Note over p_A,p_B: recursive call to gather omitted
				else
				p_A-->>p_B: StreamState@A.OFF
				end
				""".strip(),
				mermaidAt( source, "if( valid )" ) );
	}

	@Test
	public void localHelperMethodsAreExpandedAtCallSites() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Checkout@( A, B, C ) {
				SymChannel@( A, B )< Object > ab;
				SymChannel@( B, C )< Object > bc;

				void run( String@A order, Boolean@B ready, String@B shipment ) {
					receive( order );
					this.ship( ready, shipment );
				}

				private void receive( String@A order ) {
					ab.< String >com( order );
				}

				private void ship( Boolean@B ready, String@B shipment ) {
					if( ready ) {
						notifyWarehouse( shipment );
					} else {
						bc.< String >com( shipment );
					}
				}

				private void notifyWarehouse( String@B shipment ) {
					bc.< String >com( shipment );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				participant p_C as C
				Note over p_A,p_C: Checkout.run
				Note over p_A,p_C: call receive
				p_A->>p_B: order
				Note over p_A,p_C: return receive
				Note over p_A,p_C: call ship
				alt ready
				Note over p_A,p_C: call notifyWarehouse
				p_B->>p_C: shipment
				Note over p_A,p_C: return notifyWarehouse
				else
				p_B->>p_C: shipment
				end
				Note over p_A,p_C: return ship
				""".strip(),
				mermaidAt( source, "void run" ) );
	}

	@Test
	public void helperArgumentsAreVisitedBeforeTheHelperBody() {
		String source =
			"""
			import choral.channels.SymChannel;

			class EvaluationOrder@( A, B ) {
				SymChannel@( A, B )< Object > forward;
				SymChannel@( B, A )< Object > reverse;

				void run( String@A value ) {
					respond( forward.< String >com( value ) );
				}

				private void respond( String@B received ) {
					reverse.< String >com( received );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: EvaluationOrder.run
				p_A->>p_B: value
				Note over p_A,p_B: call respond
				p_B->>p_A: received
				Note over p_A,p_B: return respond
				""".strip(),
				mermaidAt( source, "void run" ) );
	}

	@Test
	public void methodsOnExternalObjectsAreExpanded() {
		String source =
			"""
			import choral.channels.SymChannel;

			class External@( A, B ) {
				void send(
						SymChannel@( A, B )< Object > channel,
						String@A value ) {
					channel.< String >com( value );
				}
			}

			class Root@( A, B ) {
				void run(
						External@( A, B ) external,
						SymChannel@( A, B )< Object > channel,
						String@A value ) {
					external.send( channel, value );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: Root.run
				Note over p_A,p_B: call External.send
				p_A->>p_B: value
				Note over p_A,p_B: return External.send
				""".strip(),
				mermaidAt( source, "external.send" ) );
	}

	@Test
	public void externalMethodWorldsAreGroundedAtTheCallSite() {
		String source =
			"""
			import choral.channels.SymChannel;

			class External@( Sender, Receiver ) {
				void send(
						SymChannel@( Sender, Receiver )< Object > channel,
						String@Sender value ) {
					channel.< String >com( value );
				}
			}

			class Root@( A, B ) {
				void run(
						External@( B, A ) external,
						SymChannel@( B, A )< Object > channel,
						String@B value ) {
					external.send( channel, value );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: Root.run
				Note over p_A,p_B: call External.send
				p_B->>p_A: value
				Note over p_A,p_B: return External.send
				""".strip(),
				mermaidAt( source, "external.send" ) );
	}

	@Test
	public void externalSelectionLabelsUseGroundedWorlds() {
		String source =
			"""
			import choral.channels.SymChannel;

			enum Decision@X { READY }

			class External@( Sender, Receiver ) {
				void decide( SymChannel@( Sender, Receiver )< Object > channel ) {
					channel.< Decision >select( Decision@Sender.READY );
				}
			}

			class Root@( A, B ) {
				void run(
						External@( B, A ) external,
						SymChannel@( B, A )< Object > channel ) {
					external.decide( channel );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: Root.run
				Note over p_A,p_B: call External.decide
				p_B-->>p_A: Decision@B.READY
				Note over p_A,p_B: return External.decide
				""".strip(),
				mermaidAt( source, "external.decide" ) );
	}

	@Test
	public void inheritedSourceMethodsAreExpandedWithGroundedWorlds() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Base@( Sender, Receiver ) {
				public void send(
						SymChannel@( Sender, Receiver )< Object > channel,
						String@Sender value ) {
					channel.< String >com( value );
				}
			}

			class Child@( Left, Right ) extends Base@( Left, Right ) {}

			class Root@( A, B ) {
				void run(
						Child@( B, A ) child,
						SymChannel@( B, A )< Object > channel,
						String@B value ) {
					child.send( channel, value );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: Root.run
				Note over p_A,p_B: call Base.send
				p_B->>p_A: value
				Note over p_A,p_B: return Base.send
				""".strip(),
				mermaidAt( source, "child.send" ) );
	}

	@Test
	public void staticSourceMethodsAreExpanded() {
		String source =
			"""
			import choral.channels.SymChannel;

			class External@( Sender, Receiver ) {
				static void send(
						SymChannel@( Sender, Receiver )< Object > channel,
						String@Sender value ) {
					channel.< String >com( value );
				}
			}

			class Root@( A, B ) {
				void run(
						SymChannel@( B, A )< Object > channel,
						String@B value ) {
					External@( B, A ).send( channel, value );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: Root.run
				Note over p_A,p_B: call External.send
				p_B->>p_A: value
				Note over p_A,p_B: return External.send
				""".strip(),
				mermaidAt( source, "External@( B, A ).send" ) );
	}

	@Test
	public void externalOverloadsUseTheResolvedSourceDefinition() {
		String source =
			"""
			import choral.channels.SymChannel;

			class External@( Sender, Receiver ) {
				void send(
						SymChannel@( Sender, Receiver )< Object > channel,
						String@Sender value ) {
					channel.< String >com( value );
				}

				void send(
						SymChannel@( Receiver, Sender )< Object > channel,
						String@Receiver value ) {
					channel.< String >com( value );
				}
			}

			class Root@( A, B ) {
				void run(
						External@( A, B ) external,
						SymChannel@( A, B )< Object > forward,
						SymChannel@( B, A )< Object > reverse,
						String@A fromA,
						String@B fromB ) {
					external.send( forward, fromA );
					external.send( reverse, fromB );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: Root.run
				Note over p_A,p_B: call External.send(SymChannel@( A, B ) Object , String@( A ))
				p_A->>p_B: value
				Note over p_A,p_B: return External.send(SymChannel@( A, B ) Object , String@( A ))
				Note over p_A,p_B: call External.send(SymChannel@( B, A ) Object , String@( B ))
				p_B->>p_A: value
				Note over p_A,p_B: return External.send(SymChannel@( B, A ) Object , String@( B ))
				""".strip(),
				mermaidAt( source, "void run" ) );
	}

	@Test
	public void importedSourceMethodsAreExpandedAcrossCompilationUnits() {
		String helper =
			"""
			package helpers;

			import choral.channels.SymChannel;

			public class Helper@( Sender, Receiver ) {
				public void send(
						SymChannel@( Sender, Receiver )< Object > channel,
						String@Sender value ) {
					channel.< String >com( value );
				}
			}
			""";
		String root =
			"""
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

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: Root.run
				Note over p_A,p_B: call Helper.send
				p_B->>p_A: value
				Note over p_A,p_B: return Helper.send
				""".strip(),
				mermaidAt( List.of( helper, root ), 1, "helper.send" ) );
	}

	@Test
	public void crossClassRecursionStopsAtTheSharedActiveCallSet() {
		String source =
			"""
			import choral.channels.SymChannel;

			class First@( A, B ) {
				void start(
						First@( A, B ) first,
						Second@( A, B ) second,
						SymChannel@( A, B )< Object > channel,
						String@A value ) {
					second.forward( first, second, channel, value );
				}
			}

			class Second@( A, B ) {
				void forward(
						First@( A, B ) first,
						Second@( A, B ) second,
						SymChannel@( A, B )< Object > channel,
						String@A value ) {
					channel.< String >com( value );
					first.start( first, second, channel, value );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: First.start
				Note over p_A,p_B: call Second.forward
				p_A->>p_B: value
				Note over p_A,p_B: recursive call to start omitted
				Note over p_A,p_B: return Second.forward
				""".strip(),
				mermaidAt( source, "void start" ) );
	}

	@Test
	public void overloadedHelpersAreResolvedFromTypedMethodAnnotations() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Overloaded@( A, B ) {
				SymChannel@( A, B )< Object > forward;
				SymChannel@( B, A )< Object > reverse;

				void run( String@A fromA, String@B fromB ) {
					send( fromA );
					send( fromB );
				}

				private void send( String@A value ) {
					forward.< String >com( value );
				}

				private void send( String@B value ) {
					reverse.< String >com( value );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: Overloaded.run
				Note over p_A,p_B: call send(String@( A ))
				p_A->>p_B: value
				Note over p_A,p_B: return send(String@( A ))
				Note over p_A,p_B: call send(String@( B ))
				p_B->>p_A: value
				Note over p_A,p_B: return send(String@( B ))
				""".strip(),
				mermaidAt( source, "void run" ) );
	}

	@Test
	public void overloadedRootMethodsAreIdentifiedBySignature() {
		String source =
			"""
			class OverloadedRoot@( A, B ) {
				void run( String@A value ) {}
				void run( String@B value ) {}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: OverloadedRoot.run(String@( A ))
				""".strip(),
				mermaidAt( source, "String@A value" ) );
		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: OverloadedRoot.run(String@( B ))
				""".strip(),
				mermaidAt( source, "String@B value" ) );
	}

	@Test
	public void recursiveHelperExpansionStopsWithANote() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Recursive@( A, B ) {
				SymChannel@( A, B )< Object > channel;

				void run( String@A value ) {
					channel.< String >com( value );
					run( value );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: Recursive.run
				p_A->>p_B: value
				Note over p_A,p_B: recursive call to run omitted
				""".strip(),
				mermaidAt( source, "void run" ) );
	}

	@Test
	public void mutuallyRecursiveHelperExpansionStopsWithANote() {
		String source =
			"""
			import choral.channels.SymChannel;

			class MutuallyRecursive@( A, B ) {
				SymChannel@( A, B )< Object > channel;

				void run( String@A value ) {
					first( value );
				}

				private void first( String@A value ) {
					channel.< String >com( value );
					second( value );
				}

				private void second( String@A value ) {
					first( value );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: MutuallyRecursive.run
				Note over p_A,p_B: call first
				p_A->>p_B: value
				Note over p_A,p_B: call second
				Note over p_A,p_B: recursive call to first omitted
				Note over p_A,p_B: return second
				Note over p_A,p_B: return first
				""".strip(),
				mermaidAt( source, "void run" ) );
	}

	@Test
	public void helperDepthLimitPreservesBalancedControlFlow() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Deep@( A, B ) {
				SymChannel@( A, B )< Object > channel;

				void run( Boolean@A ready, String@A value ) {
					guarded( ready, value );
				}

				private void guarded( Boolean@A ready, String@A value ) {
					if( ready ) {
						deeper( value );
						deeper( value );
					} else {
						channel.< String >com( value );
					}
				}

				private void deeper( String@A value ) {
					channel.< String >com( value );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: Deep.run
				Note over p_A,p_B: call guarded
				alt ready
				Note over p_A,p_B: helper expansion depth limit 1 reached - deeper helper calls omitted
				else
				p_A->>p_B: value
				end
				Note over p_A,p_B: return guarded
				""".strip(),
				mermaidWithLimits( source, 1, 10 ) );
	}

	@Test
	public void helperExpansionCountIsBoundedDeterministically() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Repeated@( A, B ) {
				SymChannel@( A, B )< Object > channel;

				void run( String@A value ) {
					send( value );
					send( value );
					send( value );
					send( value );
				}

				private void send( String@A value ) {
					channel.< String >com( value );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: Repeated.run
				Note over p_A,p_B: call send
				p_A->>p_B: value
				Note over p_A,p_B: return send
				Note over p_A,p_B: call send
				p_A->>p_B: value
				Note over p_A,p_B: return send
				Note over p_A,p_B: helper expansion count limit 2 reached - remaining helper calls omitted
				""".strip(),
				mermaidWithLimits( source, 10, 2 ) );
	}

	@Test
	public void onlyTheMethodAtTheCursorIsRendered() {
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
				Note over p_A,p_B: ChannelSources.fromField
				p_A->>p_B: value
				""".strip(),
				mermaidAt( source, "fromField" ) );
		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: ChannelSources.fromParameter
				p_B->>p_A: value
				""".strip(),
				mermaidAt( source, "parameter.< String >com" ) );
		assertNoChoreographyAt( source, "field;" );
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
				Note over p_A,p_B: Typed.run
				p_A->>p_B: value
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
				Note over p_A,p_B: Constructed.run
				""".strip(),
				mermaidAt( source, "void run" ) );
		assertNoChoreographyAt( source, "public Constructed" );
	}

	@Test
	public void participantIdentifiersAreSanitized() {
		String source =
			"""
			class International@( Customer$EU, Seller ) {
				void run() {}
			}
			""";

		assertEquals(
				"""
				sequenceDiagram
				participant p_Customer_EU as Customer$EU
				participant p_Seller as Seller
				Note over p_Customer_EU,p_Seller: International.run
				""".strip(),
				mermaidAt( source, "void run" ) );
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
				Note over p_X,p_Y: Second.run
				p_Y-->>p_X: Decision@Y.ACCEPT
				""".strip(),
				mermaidAt( source, "secondChannel.< Decision >select" ) );
	}

	@Test
	public void selectsMethodsOnlyInsideTheirSourceRanges() {
		String source =
			"""
			/* before */ class First@( A, B ) { void first() {} } /* between */ class Second@( X, Y ) { void second() {} } /* after */
			""";

		assertNoChoreographyAt( source, "before" );
		assertNoChoreographyAt( source, "First" );
		assertEquals(
			"""
				sequenceDiagram
				participant p_A as A
				participant p_B as B
				Note over p_A,p_B: First.first
				""".strip(),
				mermaidAt( source, "first()" ) );
		assertNoChoreographyAt( source, "between" );
		assertNoChoreographyAt( source, "Second" );
		assertEquals(
			"""
				sequenceDiagram
				participant p_X as X
				participant p_Y as Y
				Note over p_X,p_Y: Second.second
				""".strip(),
				mermaidAt( source, "second()" ) );
		assertNoChoreographyAt( source, "after" );
	}

	private static String mermaid( String source, int line, int character ) {
		try {
			var unit = Parser.parseString( source );
			Typer.annotate(
					List.of( unit ), HeaderLoader.loadStandardProfile().toList(),
					new TyperOptions( VerbosityLevel.WARNINGS ) );
			return new ChoreographyDiagramProvider().diagram(
					unit, new Position( line, character ) );
		} catch( ChoreographyDiagramException exception ) {
			throw exception;
		} catch( Exception exception ) {
			throw new RuntimeException( exception );
		}
	}

	private static String mermaid(
			List<String> sources, int unitIndex, int line, int character ) {
		try {
			var units = sources.stream().map(source -> Parser.parseString(source)).toList();
			Typer.annotate(
					units, HeaderLoader.loadStandardProfile().toList(),
					new TyperOptions( VerbosityLevel.WARNINGS ) );
			return new ChoreographyDiagramProvider().diagram(
					units.get( unitIndex ), new Position( line, character ) );
		} catch( ChoreographyDiagramException exception ) {
			throw exception;
		} catch( Exception exception ) {
			throw new RuntimeException( exception );
		}
	}

	private static String mermaidWithLimits(
			String source, int maximumHelperDepth, int maximumHelperExpansions ) {
		try {
			var unit = Parser.parseString( source );
			Typer.annotate(
					List.of( unit ), HeaderLoader.loadStandardProfile().toList(),
					new TyperOptions( VerbosityLevel.WARNINGS ) );
			var declaration = unit.classes().get( 0 );
			return new MermaidVisitor( maximumHelperDepth, maximumHelperExpansions )
					.render( declaration, declaration.methods().get( 0 ) );
		} catch( Exception exception ) {
			throw new RuntimeException( exception );
		}
	}

	private static void assertNoChoreographyAt( String source, String marker ) {
		ChoreographyDiagramException exception = assertThrows(
				ChoreographyDiagramException.class,
				() -> mermaidAt( source, marker ) );
		assertEquals( ChoreographyDiagramException.Reason.NO_SYMBOL, exception.reason() );
	}

	private static String mermaidAt( String source, String marker ) {
		return mermaidAt( List.of( source ), 0, marker );
	}

	private static String mermaidAt(
			List<String> sources, int unitIndex, String marker ) {
		String source = sources.get( unitIndex );
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
		return sources.size() == 1
				? mermaid( source, line, character )
				: mermaid( sources, unitIndex, line, character );
	}
}
