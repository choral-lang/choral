import choral.ast.CompilationUnit;
import choral.ast.expression.MethodCallExpression;
import choral.ast.expression.ScopedExpression;
import choral.ast.statement.ExpressionStatement;
import choral.compiler.HeaderLoader;
import choral.compiler.Parser;
import choral.compiler.Typer;
import choral.compiler.TyperOptions;
import choral.diagrams.ChoreographyDiagramProvider;
import choral.diagrams.ChoreographyDiagramProvider.Position;
import choral.diagrams.MermaidVisitor;
import choral.utils.VerbosityLevel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MermaidTest {
	////////// BASIC DIAGRAMS //////////

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
				participant p0 as Customer
				participant p1 as Seller
				Note over p0,p1: Empty.run
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
				participant p0 as Customer
				participant p1 as Seller
				Note over p0,p1: Order.run
				p0->>p1: order
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
				participant p0 as Customer
				participant p1 as Seller
				Note over p0,p1: Approval.run
				p0-->>p1: Decision@Customer.ACCEPT
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Escaped.run
				p0->>p1: "ready go % end"@A
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
				participant p0 as Customer
				participant p1 as Seller
				participant p2 as Shipper
				Note over p0,p2: Shipping.run
				p0->>p1: order
				p1->>p2: received
				p2-->>p1: State@Shipper.DONE
				""".strip(),
					mermaid( source, 10, 10 ) );
	}

	////////// EXPRESSION EVALUATION ORDER //////////

	@Test
	public void computedReceiverIsEvaluatedBeforeOuterCommunication() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Evaluation@( A, B ) {
				SymChannel@( A, B )< Object > route(
						SymChannel@( A, B )< Object > channel,
						String@A before ) {
					channel.< String >com( before );
					return channel;
				}

				void run(
						SymChannel@( A, B )< Object > channel,
						String@A before,
						String@A after ) {
					this.route( channel, before ).< String >com( after );
				}
			}
			""";

		assertEquals(
				"""
					sequenceDiagram
					participant p0 as A
					participant p1 as B
					Note over p0,p1: Evaluation.run
					rect rgba(0, 0, 0, 0.05)
					Note left of p0: call route
					p0->>p1: before
					end
					p0->>p1: after
					""".strip(),
				mermaidAt( source, "this.route" ) );
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Nested.run
				alt true@A
				p0-->>p1: Choice@A.THEN
				p0->>p1: value
				else
				p0-->>p1: Choice@A.ELSE
				p0->>p1: value
				end
				""".strip(),
				mermaidAt( source, "consume( channel" ) );
	}

	////////// CONTROL FLOW //////////

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
				participant p0 as A
				participant p1 as B
				participant p2 as C
				Note over p0,p2: Alternatives.run
				alt true@A
				p0->>p1: a
				alt false@B
				p1->>p2: b
				else
				p2->>p0: c
				end
				else
				p0->>p1: a
				end
				p1->>p2: b
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
				participant p0 as A
				participant p1 as B
				participant p2 as C
				Note over p0,p2: Routing.run
				alt route = FAST
				p0->>p1: a
				else route = SAFE
				p1->>p2: b
				else default
				p2->>p0: c
				end
				p0->>p1: a
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
				participant p0 as A
				participant p1 as B
				participant p2 as C
				Note over p0,p2: Recovery.run
				critical try
				p0->>p1: a
				option catch Exception@( B ) first
				p1->>p2: b
				option catch Exception@( C ) second
				p2->>p0: c
				end
				p1->>p2: b
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: EmptyControlFlow.run
				p0->>p1: value
				""".strip(),
				mermaidAt( source, "void run" ) );
	}

	@Test
	public void mixedEmptyControlFlowBranchesRemainVisible() {
		String source =
			"""
			import choral.channels.SymChannel;

			enum Choice@X { FIRST }

			class Partial@( A, B ) {
				void run(
						SymChannel@( A, B )< Object > forward,
						SymChannel@( B, A )< Object > reverse,
						Boolean@A ready,
						Choice@A choice,
						String@A fromA,
						String@B fromB ) {
					if( ready ) {} else { forward.< String >com( fromA ); }
					switch( choice ) {
						case FIRST -> {}
						default -> { reverse.< String >com( fromB ); }
					}
					try {} catch( Exception@A error ) {
						forward.< String >com( fromA );
					}
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Partial.run
				alt ready
				else
				p0->>p1: fromA
				end
				alt choice = FIRST
				else default
				p1->>p0: fromB
				end
				critical try
				option catch Exception@( A ) error
				p0->>p1: fromA
				end
				""".strip(),
			mermaidAt( source, "if( ready )" ) );
	}

	@Test
	public void guardCommunicationsPrecedeControlFlowBlocks() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Guard@( A, B ) {
				void run(
						SymChannel@( A, B )< Object > forward,
						SymChannel@( B, A )< Object > reverse,
						Boolean@A ready,
						String@B value ) {
					if( forward.< Boolean >com( ready ) ) {
						reverse.< String >com( value );
					}
				}
			}
			""";
		String diagram = mermaidAt( source, "if( forward" );
		int guard = diagram.indexOf( "p0->>p1: ready" );
		int alternative = diagram.indexOf( "\nalt " );
		int branch = diagram.indexOf( "p1->>p0: value" );

		assertTrue( guard >= 0 && guard < alternative );
		assertTrue( alternative < branch );
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Streaming.gather
				alt streaming
				p0->>p1: value
				Note over p0,p1: recursive call to gather omitted
				else
				p0-->>p1: StreamState@A.OFF
				end
				""".strip(),
				mermaidAt( source, "if( valid )" ) );
	}

	////////// HELPER EXPANSION //////////

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
				participant p0 as A
				participant p1 as B
				participant p2 as C
				Note over p0,p2: Checkout.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call receive
				p0->>p1: order
				end
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call ship
				alt ready
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call notifyWarehouse
				p1->>p2: shipment
				end
				else
				p1->>p2: shipment
				end
				end
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: EvaluationOrder.run
				p0->>p1: value
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call respond
				p1->>p0: received
				end
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Root.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call External.send
				p0->>p1: value
				end
				""".strip(),
				mermaidAt( source, "external.send" ) );
	}

	////////// WORLD SUBSTITUTION //////////

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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Root.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call External.send
				p1->>p0: value
				end
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Root.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call External.decide
				p1-->>p0: Decision@B.READY
				end
				""".strip(),
				mermaidAt( source, "external.decide" ) );
	}

	@Test
	public void helperWorldLabelsAreGroundedInOnePass() {
		String source =
			"""
			import choral.channels.SymChannel;

			enum Decision@X { FIRST, SECOND }

			class External@( A, B ) {
				void decide(
						SymChannel@( A, B )< Object > forward,
						SymChannel@( B, A )< Object > reverse ) {
					forward.< Decision >select( Decision@A.FIRST );
					reverse.< Decision >select( Decision@B.SECOND );
				}
			}

			class Root@( A, B ) {
				void run(
						External@( B, A ) external,
						SymChannel@( B, A )< Object > reverse,
						SymChannel@( A, B )< Object > forward ) {
					external.decide( reverse, forward );
					forward.< Decision >select( Decision@A.FIRST );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Root.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call External.decide
				p1-->>p0: Decision@B.FIRST
				p0-->>p1: Decision@A.SECOND
				end
				p0-->>p1: Decision@A.FIRST
				""".strip(),
				mermaidAt( source, "external.decide" ) );
	}

	@Test
	public void helperWorldLiteralLabelsAreGroundedInOnePass() {
		String source =
			"""
			import choral.channels.SymChannel;

			class External@( A, B ) {
				void send(
						SymChannel@( A, B )< Object > forward,
						SymChannel@( B, A )< Object > reverse ) {
					forward.< String >com( "first"@A );
					reverse.< String >com( "second"@B );
				}
			}

			class Root@( A, B ) {
				void run(
						External@( B, A ) external,
						SymChannel@( B, A )< Object > reverse,
						SymChannel@( A, B )< Object > forward ) {
					external.send( reverse, forward );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Root.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call External.send
				p1->>p0: "first"@B
				p0->>p1: "second"@A
				end
				""".strip(),
				mermaidAt( source, "external.send" ) );
	}

	@Test
	public void helperCatchLabelsUseGroundedParenthesizedWorlds() {
		String source =
			"""
			import choral.channels.SymChannel;

			class External@( A, B ) {
				void recover(
						SymChannel@( A, B )< Object > channel,
						String@A value ) {
					try {
					} catch( Exception@A error ) {
						channel.< String >com( value );
					}
				}
			}

			class Root@( A, B ) {
				void run(
						External@( B, A ) external,
						SymChannel@( B, A )< Object > channel,
						String@B value ) {
					external.recover( channel, value );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Root.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call External.recover
				critical try
				option catch Exception@( B ) error
				p1->>p0: value
				end
				end
				""".strip(),
				mermaidAt( source, "external.recover" ) );
	}

	@Test
	public void helperInstantiationArgumentsUseGroundedParenthesizedWorlds() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Payload@X {
				public Payload() {}
			}

			class External@( A, B ) {
				void send( SymChannel@( A, B )< Object > channel ) {
					channel.< Payload >com( new Payload@A() );
				}
			}

			class Root@( A, B ) {
				void run(
						External@( B, A ) external,
						SymChannel@( B, A )< Object > channel ) {
					external.send( channel );
				}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Root.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call External.send
				p1->>p0: new Payload@( B )()
				end
				""".strip(),
				mermaidAt( source, "external.send" ) );
	}

	@Test
	public void helperExpansionRejectsMismatchedWorldArity() {
		var unit = typedUnits( List.of(
			"""
			class One@X {
				void placeholder() {}
			}

			class External@( A, B ) {
				void send() {}
			}

			class Root@( A, B ) {
				void run( External@( A, B ) external ) {
					external.send();
				}
			}
			""" ) ).get( 0 );
		var externalMethod = unit.classes().get( 1 ).methods().get( 0 );
		var mismatchedAnnotation = unit.classes().get( 0 ).methods().get( 0 )
				.typeAnnotation().orElseThrow();
		// The typer prevents this mismatch, so pair two real typed halves to test the guard.
		mismatchedAnnotation.setSourceCode( externalMethod );
		var root = unit.classes().get( 2 );
		var statement = assertInstanceOf( ExpressionStatement.class,
				root.methods().get( 0 ).body().orElseThrow() );
		var scoped = assertInstanceOf( ScopedExpression.class, statement.expression() );
		var call = assertInstanceOf( MethodCallExpression.class, scoped.scopedExpression() );
		call.setMethodAnnotation( mismatchedAnnotation.innerCallable() );

		var exception = assertThrows( IllegalStateException.class,
				() -> MermaidVisitor.render( root, root.methods().get( 0 ) ) );

		assertEquals(
				"World arity mismatch while expanding method 'send': expected 2 but resolved 1",
				exception.getMessage() );
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Root.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call Base.send
				p1->>p0: value
				end
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Root.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call External.send
				p1->>p0: value
				end
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Root.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call External.send(SymChannel@( A, B ) Object , String@( A ))
				p0->>p1: value
				end
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call External.send(SymChannel@( B, A ) Object , String@( B ))
				p1->>p0: value
				end
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Root.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call Helper.send
				p1->>p0: value
				end
				""".strip(),
				diagramAt( List.of( helper, root ), 1, "helper.send" ).orElseThrow() );
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: First.start
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call Second.forward
				p0->>p1: value
				Note over p0,p1: recursive call to start omitted
				end
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Overloaded.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call send(String@( A ))
				p0->>p1: value
				end
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call send(String@( B ))
				p1->>p0: value
				end
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: OverloadedRoot.run(String@( A ))
				""".strip(),
				mermaidAt( source, "String@A value" ) );
		assertEquals(
			"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: OverloadedRoot.run(String@( B ))
				""".strip(),
				mermaidAt( source, "String@B value" ) );
	}

	////////// RECURSION AND SAFETY LIMITS //////////

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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Recursive.run
				p0->>p1: value
				Note over p0,p1: recursive call to run omitted
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: MutuallyRecursive.run
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call first
				p0->>p1: value
				rect rgba(0, 0, 0, 0.05)
				Note left of p0: call second
				Note over p0,p1: recursive call to first omitted
				end
				end
				""".strip(),
				mermaidAt( source, "void run" ) );
	}

	@Test
	public void helperExpansionHasNoDepthLimit() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Deep@( A, B ) {
				SymChannel@( A, B )< Object > channel;
				String@A value;

				void run() {
					helper0();
				}

				private void helper0() { helper1(); }
				private void helper1() { helper2(); }
				private void helper2() { helper3(); }
				private void helper3() { helper4(); }
				private void helper4() { helper5(); }
				private void helper5() { helper6(); }
				private void helper6() { helper7(); }
				private void helper7() { helper8(); }
				private void helper8() { helper9(); }
				private void helper9() { helper10(); }
				private void helper10() { helper11(); }
				private void helper11() { helper12(); }
				private void helper12() { helper13(); }
				private void helper13() { helper14(); }
				private void helper14() { helper15(); }
				private void helper15() { helper16(); }
				private void helper16() { channel.< String >com( value ); }
			}
			""";

		String mermaid = mermaidAt( source, "void run" );

		assertTrue( mermaid.contains( "Note left of p0: call helper16" ) );
		assertTrue( mermaid.contains( "p0->>p1: value" ) );
		assertFalse( mermaid.contains( "helper expansion depth limit" ) );
	}

	@Test
	public void helperExpansionCountIsBoundedDeterministically() {
		String calls = "send( value );\n".repeat( 129 );
		String source =
			"""
			import choral.channels.SymChannel;

			class Repeated@( A, B ) {
				SymChannel@( A, B )< Object > channel;

				void run( String@A value ) {
					%s
				}

				private void send( String@A value ) {
					channel.< String >com( value );
				}
			}
			""".formatted( calls );

		String mermaid = mermaidAt( source, "void run" );

		assertEquals( 128, mermaid.lines()
				.filter( "Note left of p0: call send"::equals )
				.count() );
		assertTrue( mermaid.contains(
				"helper expansion count limit 128 reached - remaining helper calls omitted" ) );
	}

	@Test
	public void separateStaticRendersDoNotShareState() {
		String source =
			"""
			import choral.channels.SymChannel;

			class Separate@( A, B ) {
				void forward(
						SymChannel@( A, B )< Object > channel,
						String@A value ) {
					channel.< String >com( value );
				}

				void reverse(
						SymChannel@( B, A )< Object > channel,
						String@B value ) {
					channel.< String >com( value );
				}
			}
			""";
		var unit = typedUnits( List.of( source ) ).get( 0 );
		var declaration = unit.classes().get( 0 );

		assertEquals(
				"""
					sequenceDiagram
					participant p0 as A
					participant p1 as B
					Note over p0,p1: Separate.forward
					p0->>p1: value
					""".strip(),
				MermaidVisitor.render( declaration, declaration.methods().get( 0 ) ) );
		assertEquals(
				"""
					sequenceDiagram
					participant p0 as A
					participant p1 as B
					Note over p0,p1: Separate.reverse
					p1->>p0: value
					""".strip(),
				MermaidVisitor.render( declaration, declaration.methods().get( 1 ) ) );
		assertThrows( NullPointerException.class,
				() -> MermaidVisitor.render( null, declaration.methods().get( 0 ) ) );
		assertThrows( NullPointerException.class,
				() -> MermaidVisitor.render( declaration, null ) );
	}

	////////// TYPED CALL DETECTION //////////

	@Test
	public void renderingAnUntypedOrdinaryCallFailsClearly() {
		var unit = Parser.parseString(
			"""
			class Untyped@( A, B ) {
				void run() {
					helper();
				}

				private void helper() {}
			}
			""" );
		var declaration = unit.classes().get( 0 );

		var exception = assertThrows( IllegalStateException.class,
				() -> MermaidVisitor.render( declaration, declaration.methods().get( 0 ) ) );

		assertEquals( "Method call has no resolved method annotation: helper",
				exception.getMessage() );
	}

	@Test
	public void rendererRejectsAMethodOwnedByAnotherDeclaration() {
		var unit = typedUnits( List.of(
			"""
			class First@( A, B ) {
				void first() {}
			}

			class Second@( A, B ) {
				void second() {}
			}
			""" ) ).get( 0 );
		var first = unit.classes().get( 0 );
		var secondMethod = unit.classes().get( 1 ).methods().get( 0 );

		var exception = assertThrows( IllegalArgumentException.class,
				() -> MermaidVisitor.render( first, secondMethod ) );

		assertEquals( "Method 'second' does not belong to declaration 'First'",
				exception.getMessage() );
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: ChannelSources.fromField
				p0->>p1: value
				""".strip(),
				mermaidAt( source, "fromField" ) );
		assertEquals(
			"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: ChannelSources.fromParameter
				p1->>p0: value
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Typed.run
				p0->>p1: value
				""".strip(),
				mermaidAt( source, "arbitrarilyNamed.< String >com" ) );
	}

	@Test
	public void channelCallsOnTypedComputedReceiversBecomeEvents() {
		String source =
			"""
			import choral.channels.SymChannel;

			class ComputedReceiver@( A, B ) {
				SymChannel@( A, B )< Object > channel(
						SymChannel@( A, B )< Object > value ) {
					return value;
				}

				void run(
						SymChannel@( A, B )< Object > channel,
						String@A value ) {
					this.channel( channel ).< String >com( value );
				}
			}
			""";

		assertEquals(
				"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: ComputedReceiver.run
				p0->>p1: value
				""".strip(),
				mermaidAt( source, "this.channel" ) );
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Constructed.run
				""".strip(),
				mermaidAt( source, "void run" ) );
		assertNoChoreographyAt( source, "public Constructed" );
	}

	////////// SOURCE SELECTION //////////

	@Test
	public void distinctWorldNamesReceiveDistinctParticipantIds() {
		String source =
			"""
			class Identifiers@( A$B, A_B ) {
				void run() {}
			}
			""";

		assertEquals(
			"""
				sequenceDiagram
				participant p0 as A$B
				participant p1 as A_B
				Note over p0,p1: Identifiers.run
				""".strip(),
			mermaidAt( source, "void run" ) );
	}

	@Test
	public void participantIdentifiersFollowDeclarationOrder() {
		String source =
			"""
			class International@( Customer$EU, Seller ) {
				void run() {}
			}
			""";

		assertEquals(
				"""
				sequenceDiagram
				participant p0 as Customer$EU
				participant p1 as Seller
				Note over p0,p1: International.run
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
				participant p0 as X
				participant p1 as Y
				Note over p0,p1: Second.run
				p1-->>p0: Decision@Y.ACCEPT
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
				participant p0 as A
				participant p1 as B
				Note over p0,p1: First.first
				""".strip(),
				mermaidAt( source, "first()" ) );
		assertNoChoreographyAt( source, "between" );
		assertNoChoreographyAt( source, "Second" );
		assertEquals(
			"""
				sequenceDiagram
				participant p0 as X
				participant p1 as Y
				Note over p0,p1: Second.second
				""".strip(),
				mermaidAt( source, "second()" ) );
		assertNoChoreographyAt( source, "after" );
	}

	///////////////////// TEST HARNESS /////////////////////

	private static String mermaid( String source, int line, int character ) {
		return diagram( source, line, character ).orElseThrow();
	}

	private static Optional<String> diagram( String source, int line, int character ) {
		try {
			var unit = typedUnits( List.of( source ) ).get( 0 );
			return new ChoreographyDiagramProvider().diagram(
					unit, new Position( line, character ) );
		} catch( Exception exception ) {
			throw new RuntimeException( exception );
		}
	}

	private static String mermaid(
			List<String> sources, int unitIndex, int line, int character ) {
		return diagram( sources, unitIndex, line, character ).orElseThrow();
	}

	private static Optional<String> diagram(
			List<String> sources, int unitIndex, int line, int character ) {
		try {
			var units = typedUnits( sources );
			return new ChoreographyDiagramProvider().diagram(
					units.get( unitIndex ), new Position( line, character ) );
		} catch( Exception exception ) {
			throw new RuntimeException( exception );
		}
	}

	private static List<CompilationUnit> typedUnits( List<String> sources ) {
		try {
			var units = sources.stream().map( Parser::parseString ).toList();
			Typer.annotate(
					units, HeaderLoader.loadStandardProfile().toList(),
					new TyperOptions( VerbosityLevel.WARNINGS ) );
			return units;
		} catch( Exception exception ) {
			throw new RuntimeException( exception );
		}
	}

	private static void assertNoChoreographyAt( String source, String marker ) {
		assertTrue( diagramAt( List.of( source ), 0, marker ).isEmpty() );
	}

	private static String mermaidAt( String source, String marker ) {
		return diagramAt( List.of( source ), 0, marker ).orElseThrow();
	}

	private static Optional<String> diagramAt(
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
				? diagram( source, line, character )
				: diagram( sources, unitIndex, line, character );
	}
}
