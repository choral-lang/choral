import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import choral.ast.CompilationUnit;
import choral.ast.expression.MethodCallExpression;
import choral.ast.expression.ScopedExpression;
import choral.ast.statement.ExpressionStatement;
import choral.types.GroundDataType;
import lsp.ChoreographyDiagramParams;
import lsp.ChoralLanguageServer;
import lsp.ChoralTextDocumentService;
import lsp.features.DiagnosticsProvider;
import lsp.features.TypedSourceAnalyzer;
import lsp.features.TypedSourceAnalyzer.AnalysisResult;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChoralTextDocumentServiceTest {
	@Test
	public void exposesTypedChoreographyRequestToJsonRpc() {
		var request = ServiceEndpoints.getSupportedMethods( ChoralLanguageServer.class )
				.get( "choral/choreographyDiagram" );

		assertNotNull( request );
		assertEquals( ChoreographyDiagramParams.class, request.getParameterTypes()[ 0 ] );
	}

	@Test
	public void typeCheckingAnnotatesTheAstUsedForDiagramExtraction( @TempDir Path project )
			throws Exception {
		String uri = project.resolve( "Example.ch" ).toUri().toString();
		String source = """
						import choral.channels.SymChannel;

						class Example@( A, B ) {
						    void run( SymChannel@( A, B )< Object > channel, String@A value ) {
						        channel.< String >com( value );
						    }
						}
						""";
		AnalysisResult analysis = new TypedSourceAnalyzer().analyze( uri, source );
		assertTrue( analysis.successful() );
		CompilationUnit typedUnit = analysis.compilationUnit();

		ExpressionStatement statement = assertInstanceOf( ExpressionStatement.class,
				typedUnit.classes().get( 0 ).methods().get( 0 ).body().orElseThrow() );
		ScopedExpression expression =
				assertInstanceOf( ScopedExpression.class, statement.expression() );
		MethodCallExpression call = assertInstanceOf( MethodCallExpression.class,
				expression.scopedExpression() );
		assertTrue( call.methodAnnotation().isPresent() );
		assertEquals( "A", call.methodAnnotation().orElseThrow().signature()
				.parameters().get( 0 ).type().worldArguments().get( 0 ).identifier() );
		assertEquals( "B", ( (GroundDataType) call.methodAnnotation().orElseThrow().returnType() )
				.worldArguments().get( 0 ).identifier() );
	}

	@Test
	public void returnsMermaidForTypedRequestParameters( @TempDir Path project ) {
		String uri = project.resolve( "Example.ch" ).toUri().toString();
		String source = """
						import choral.channels.SymChannel;

						class Example@( A, B ) {
						    SymChannel@( A, B )< Object > c;
						    void run( String@A value ) { c.< String >com( value ); }
						}
						""";
		ChoralTextDocumentService service = new ChoralTextDocumentService();
		service.didOpen(
				new DidOpenTextDocumentParams( new TextDocumentItem( uri, "choral", 1, source ) ) );

		String result = service.choreographyDiagram( new ChoreographyDiagramParams(
				new TextDocumentIdentifier( uri ), new Position( 4, 45 ) ) ).join();

		assertEquals(
				"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Example.run
				p0->>p1: value
				""".strip(),
				result );
	}

	@Test
	public void helperExpansionDepthDefaultsToZeroAndCanBeRequested( @TempDir Path project ) {
		String uri = project.resolve( "Example.ch" ).toUri().toString();
		String source = """
						import choral.channels.SymChannel;

						class Example@( A, B ) {
						    SymChannel@( A, B )< Object > channel;
						    String@A value;

						    void run() { helper(); }
						    void helper() { channel.< String >com( value ); }
						}
						""";
		ChoralTextDocumentService service = new ChoralTextDocumentService();
		service.didOpen(
				new DidOpenTextDocumentParams( new TextDocumentItem( uri, "choral", 1, source ) ) );

		assertEquals(
				"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Example.run
				Note over p0,p1: call helper
				""".strip(),
				diagramAt( service, uri, source, "void run" ).join() );
		assertEquals(
				"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Example.run
				rect rgba(0, 0, 0, 0.05)
				Note over p0,p1: call helper
				p0->>p1: value
				end
				""".strip(),
				diagramAt( service, uri, source, "void run", 1 ).join() );
	}

	@Test
	public void rendersOnlyTheMethodAtTheRequestedPosition( @TempDir Path project ) {
		String uri = project.resolve( "Example.ch" ).toUri().toString();
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
		service.didOpen(
				new DidOpenTextDocumentParams( new TextDocumentItem( uri, "choral", 1, source ) ) );

		assertEquals(
				"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Example.first
				p0->>p1: value
				""".strip(),
				diagramAt( service, uri, source, "void first" ).join() );
		assertEquals(
				"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Example.second
				p1->>p0: value
				""".strip(),
				diagramAt( service, uri, source, "reverse.< String >com" ).join() );

		assertNull( diagramAt( service, uri, source, "class Example" ).join() );
	}

	@Test
	public void reportsTypeCheckingFailures( @TempDir Path project ) {
		String uri = project.resolve( "Example.ch" ).toUri().toString();
		String source = """
						class Example@( A, B ) {
						    void run( Missing@A value ) {}
						}
						""";
		ChoralTextDocumentService service = new ChoralTextDocumentService();
		service.didOpen(
				new DidOpenTextDocumentParams( new TextDocumentItem( uri, "choral", 1, source ) ) );

		ResponseError error = diagramError( service.choreographyDiagram(
				new ChoreographyDiagramParams(
						new TextDocumentIdentifier( uri ), new Position( 1, 10 ) ) ) );

		assertEquals( ResponseErrorCode.RequestFailed.getValue(), error.getCode() );
		assertTrue( error.getMessage().startsWith( "Unable to type-check the Choral document:" ) );
	}

	@Test
	public void reportsInvalidChoreographyRequestParameters() {
		ChoralTextDocumentService service = new ChoralTextDocumentService();

		ResponseError error = diagramError( service.choreographyDiagram(
				new ChoreographyDiagramParams() ) );

		assertEquals( ResponseErrorCode.InvalidParams.getValue(), error.getCode() );
		assertEquals( "The choreography request did not include a document URI.",
				error.getMessage() );
	}

	@Test
	public void requiresAChoreographyRequestPosition() {
		ChoralTextDocumentService service = new ChoralTextDocumentService();
		ChoreographyDiagramParams params = new ChoreographyDiagramParams();
		params.setTextDocument( new TextDocumentIdentifier( "file:///Example.ch" ) );

		ResponseError error = diagramError( service.choreographyDiagram( params ) );

		assertEquals( ResponseErrorCode.InvalidParams.getValue(), error.getCode() );
		assertEquals( "The choreography request did not include a cursor position.",
				error.getMessage() );
	}

	@Test
	public void rejectsNegativeHelperExpansionDepth() {
		ChoralTextDocumentService service = new ChoralTextDocumentService();
		ChoreographyDiagramParams params = new ChoreographyDiagramParams(
				new TextDocumentIdentifier( "file:///Example.ch" ), new Position( 0, 0 ), -1 );

		ResponseError error = diagramError( service.choreographyDiagram( params ) );

		assertEquals( ResponseErrorCode.InvalidParams.getValue(), error.getCode() );
		assertEquals( "Helper expansion depth must not be negative.", error.getMessage() );
	}

	@Test
	public void reportsUnexpectedAnalysisFailuresAsInternalErrors( @TempDir Path project ) {
		String uri = project.resolve( "Example.ch" ).toUri().toString();
		String source = "class Example@( A, B ) { void run() {} }";
		TypedSourceAnalyzer analyzer = new TypedSourceAnalyzer() {
			@Override
			public AnalysisResult analyze(
					String uri, String content, java.util.Map< String, String > openDocuments
			) {
				throw new IllegalStateException( "analysis failed" );
			}
		};
		ChoralTextDocumentService service = new ChoralTextDocumentService( analyzer );
		service.didOpen(
				new DidOpenTextDocumentParams( new TextDocumentItem( uri, "choral", 1, source ) ) );

		ResponseError error = diagramError( diagramAt( service, uri, source, "run()" ) );

		assertEquals( ResponseErrorCode.InternalError.getValue(), error.getCode() );
		assertEquals( "Unable to analyze the Choral document: analysis failed",
				error.getMessage() );
	}

	@Test
	public void documentChangesReplaceTheTypedAst( @TempDir Path project ) {
		String uri = project.resolve( "Example.ch" ).toUri().toString();
		String first = "class First@( A, B ) { void first() {} }";
		String second = "class Second@( X, Y ) { void second() {} }";
		ChoralTextDocumentService service = new ChoralTextDocumentService();
		service.didOpen(
				new DidOpenTextDocumentParams( new TextDocumentItem( uri, "choral", 1, first ) ) );

		service.didChange( new DidChangeTextDocumentParams(
				new VersionedTextDocumentIdentifier( uri, 2 ),
				List.of( new TextDocumentContentChangeEvent( second ) ) ) );
		String diagram = diagramAt( service, uri, second, "second()" ).join();

		assertTrue( diagram.contains( "participant p0 as X" ) );
		assertFalse( diagram.contains( "participant p0 as A" ) );
	}

	@Test
	public void analysisResolvesProjectHeaders( @TempDir Path project ) throws Exception {
		Files.writeString( project.resolve( "ProjectValue.chh" ),
				"""
				interface ProjectValue@( X ) {
				    String@X identity( String@X value );
				}
				""" );
		String uri = project.resolve( "Example.ch" ).toUri().toString();
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
		ChoralTextDocumentService service = new ChoralTextDocumentService();
		service.didOpen(
				new DidOpenTextDocumentParams( new TextDocumentItem( uri, "choral", 1, source ) ) );

		String diagram = diagramAt( service, uri, source, "channel.< String >com" ).join();

		assertTrue( diagram.contains( "p0->>p1: local" ) );
	}

	@Test
	public void expandsImportedSourceMethodsFromTheWorkspace( @TempDir Path project )
			throws Exception {
		Files.writeString( project.resolve( "Helper.ch" ),
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
				""" );
		Files.writeString( project.resolve( "Helper.chh" ),
				"""
				package helpers;

				public interface Helper@( Sender, Receiver ) {}
				""" );
		String uri = project.resolve( "Root.ch" ).toUri().toString();
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
		service.didOpen(
				new DidOpenTextDocumentParams( new TextDocumentItem( uri, "choral", 1, source ) ) );

		assertEquals(
				"""
				sequenceDiagram
				participant p0 as A
				participant p1 as B
				Note over p0,p1: Root.run
				rect rgba(0, 0, 0, 0.05)
				Note over p0,p1: call Helper.send
				p1->>p0: value
				end
				""".strip(),
				diagramAt( service, uri, source, "helper.send", 1 ).join() );
	}

	@Test
	public void expandsImportedMethodsFromUnsavedOpenDocuments( @TempDir Path project )
			throws Exception {
		Path helperPath = project.resolve( "Helper.ch" );
		Files.writeString( helperPath,
				"""
				package helpers;

				import choral.channels.SymChannel;

				public class Helper@( Sender, Receiver ) {
				    public void send(
				            SymChannel@( Sender, Receiver )< Object > channel,
				            String@Sender diskValue ) {
				        channel.< String >com( diskValue );
				    }
				}
				""" );
		Files.writeString( project.resolve( "Helper.chh" ),
				"""
				package helpers;

				public interface Helper@( Sender, Receiver ) {}
				""" );
		String helperUri = helperPath.toUri().toString();
		String unsavedHelper = """
							   package helpers;

							   import choral.channels.SymChannel;

							   public class Helper@( Sender, Receiver ) {
							       public void send(
							               SymChannel@( Sender, Receiver )< Object > channel,
							               String@Sender unsavedValue ) {
							           channel.< String >com( unsavedValue );
							       }
							   }
							   """;
		String rootUri = project.resolve( "Root.ch" ).toUri().toString();
		String rootSource = """
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
		service.didOpen( new DidOpenTextDocumentParams(
				new TextDocumentItem( helperUri, "choral", 1, unsavedHelper ) ) );
		service.didOpen( new DidOpenTextDocumentParams(
				new TextDocumentItem( rootUri, "choral", 1, rootSource ) ) );

		String diagram = diagramAt( service, rootUri, rootSource, "helper.send", 1 ).join();

		assertTrue( diagram.contains( "p1->>p0: unsavedValue" ) );
		assertFalse( diagram.contains( "p1->>p0: diskValue" ) );

		String changedHelper = unsavedHelper.replace( "unsavedValue", "changedValue" );
		service.didChange( new DidChangeTextDocumentParams(
				new VersionedTextDocumentIdentifier( helperUri, 2 ),
				List.of( new TextDocumentContentChangeEvent( changedHelper ) ) ) );

		String updatedDiagram = diagramAt(
				service, rootUri, rootSource, "helper.send", 1 ).join();

		assertTrue( updatedDiagram.contains( "p1->>p0: changedValue" ) );
		assertFalse( updatedDiagram.contains( "p1->>p0: unsavedValue" ) );
	}

	@Test
	public void expandsSamePackageMethodsWithoutAnImport( @TempDir Path project )
			throws Exception {
		Path packageFolder = Files.createDirectories( project.resolve( "app" ) );
		Files.writeString( packageFolder.resolve( "Helper.ch" ),
				"""
				package app;

				import choral.channels.SymChannel;

				public class Helper@( Sender, Receiver ) {
				    public void send(
				            SymChannel@( Sender, Receiver )< Object > channel,
				            String@Sender value ) {
				        channel.< String >com( value );
				    }
				}
				""" );
		Files.writeString( packageFolder.resolve( "Helper.chh" ),
				"""
				package app;

				import choral.channels.SymChannel;

				public interface Helper@( Sender, Receiver ) {
				    public void send(
				            SymChannel@( Sender, Receiver )< Object > channel,
				            String@Sender value );
				}
				""" );
		String uri = packageFolder.resolve( "Root.ch" ).toUri().toString();
		String source = """
						package app;

						import choral.channels.SymChannel;

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
		service.didOpen(
				new DidOpenTextDocumentParams( new TextDocumentItem( uri, "choral", 1, source ) ) );

		String diagram = diagramAt( service, uri, source, "helper.send", 1 ).join();

		assertTrue( diagram.contains( "Note over p0,p1: call Helper.send" ) );
		assertTrue( diagram.contains( "p1->>p0: value" ) );
	}

	@Test
	public void expandsHelpersThroughCyclicSourceImports( @TempDir Path project )
			throws Exception {
		Path helpers = Files.createDirectories( project.resolve( "helpers" ) );
		Files.writeString( helpers.resolve( "First.ch" ),
				"""
				package helpers;

				import choral.channels.SymChannel;
				import helpers.Second;

				public class First@( Sender, Receiver ) {
				    public void send(
				            Second@( Sender, Receiver ) next,
				            SymChannel@( Sender, Receiver )< Object > channel,
				            String@Sender value ) {
				        next.send( channel, value );
				    }
				}
				""" );
		Files.writeString( helpers.resolve( "Second.ch" ),
				"""
				package helpers;

				import choral.channels.SymChannel;
				import helpers.First;

				public class Second@( Sender, Receiver ) {
				    public void send(
				            SymChannel@( Sender, Receiver )< Object > channel,
				            String@Sender value ) {
				        channel.< String >com( value );
				    }
				}
				""" );
		String uri = project.resolve( "Root.ch" ).toUri().toString();
		String source = """
						package app;

						import choral.channels.SymChannel;
						import helpers.First;
						import helpers.Second;

						class Root@( A, B ) {
						    void run(
						            First@( B, A ) first,
						            Second@( B, A ) second,
						            SymChannel@( B, A )< Object > channel,
						            String@B value ) {
						        first.send( second, channel, value );
						    }
						}
						""";
		ChoralTextDocumentService service = new ChoralTextDocumentService();
		service.didOpen(
				new DidOpenTextDocumentParams( new TextDocumentItem( uri, "choral", 1, source ) ) );

		String diagram = diagramAt( service, uri, source, "first.send", 2 ).join();

		assertTrue( diagram.contains( "call First.send" ) );
		assertTrue( diagram.contains( "call Second.send" ) );
		assertTrue( diagram.contains( "p1->>p0: value" ) );
	}

	@Test
	public void wildcardImportsDoNotLoadUnreferencedBrokenSources( @TempDir Path project )
			throws Exception {
		Path helpers = Files.createDirectories( project.resolve( "helpers" ) );
		Files.writeString( helpers.resolve( "Helper.ch" ),
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
				""" );
		Files.writeString( helpers.resolve( "Broken.ch" ),
				"""
				package helpers;

				public class Broken@( A ) {
				    public void fail( Missing@A value ) {}
				}
				""" );
		String uri = project.resolve( "Root.ch" ).toUri().toString();
		String source = """
						package app;

						import choral.channels.SymChannel;
						import helpers.*;

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
		service.didOpen(
				new DidOpenTextDocumentParams( new TextDocumentItem( uri, "choral", 1, source ) ) );

		String diagram = diagramAt( service, uri, source, "helper.send", 1 ).join();

		assertTrue( diagram.contains( "call Helper.send" ) );
		assertTrue( diagram.contains( "p1->>p0: value" ) );
	}

	@Test
	public void expandsInheritedMethodsFromSamePackageSources( @TempDir Path project )
			throws Exception {
		Path packageFolder = Files.createDirectories( project.resolve( "app" ) );
		Files.writeString( packageFolder.resolve( "Base.ch" ),
				"""
				package app;

				import choral.channels.SymChannel;

				public class Base@( Sender, Receiver ) {
				    public void send(
				            SymChannel@( Sender, Receiver )< Object > channel,
				            String@Sender value ) {
				        channel.< String >com( value );
				    }
				}
				""" );
		Files.writeString( packageFolder.resolve( "Base.chh" ),
				"""
				package app;

				import choral.channels.SymChannel;

				public class Base@( Sender, Receiver ) {
				    public void send(
				            SymChannel@( Sender, Receiver )< Object > channel,
				            String@Sender value );
				}
				""" );
		Files.writeString( packageFolder.resolve( "Child.ch" ),
				"""
				package app;

				public class Child@( Left, Right ) extends Base@( Left, Right ) {}
				""" );
		String uri = packageFolder.resolve( "Root.ch" ).toUri().toString();
		String source = """
						package app;

						import choral.channels.SymChannel;

						class Root@( A, B ) {
						    void run(
						            Child@( B, A ) child,
						            SymChannel@( B, A )< Object > channel,
						            String@B value ) {
						        child.send( channel, value );
						    }
						}
						""";
		ChoralTextDocumentService service = new ChoralTextDocumentService();
		service.didOpen(
				new DidOpenTextDocumentParams( new TextDocumentItem( uri, "choral", 1, source ) ) );

		String diagram = diagramAt( service, uri, source, "child.send", 1 ).join();

		assertTrue( diagram.contains( "call Base.send" ) );
		assertTrue( diagram.contains( "p1->>p0: value" ) );
	}

	@Test
	public void doesNotAttributeImportedSourceErrorsToTheActiveDocument( @TempDir Path project )
			throws Exception {
		Files.writeString( project.resolve( "Helper.ch" ),
				"""
				package helpers;

				public class Helper@( A ) {
				    public void broken( Missing@A value ) {}
				}
				""" );
		String uri = project.resolve( "Root.ch" ).toUri().toString();
		String source = """
						package app;

						import helpers.Helper;

						class Root@( A ) {
						    void run( Helper@A helper ) {}
						}
						""";
		AnalysisResult analysis = new TypedSourceAnalyzer().analyze( uri, source );

		assertFalse( analysis.successful() );
		assertTrue( new DiagnosticsProvider().diagnostics( uri, analysis ).isEmpty() );
	}

	private static CompletableFuture< String > diagramAt(
			ChoralTextDocumentService service, String uri, String source, String marker
	) {
		return diagramAt( service, uri, source, marker, 0 );
	}

	private static CompletableFuture< String > diagramAt(
			ChoralTextDocumentService service, String uri, String source, String marker,
			int helperExpansionDepth
	) {
		int offset = source.indexOf( marker );
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
		return service.choreographyDiagram( new ChoreographyDiagramParams(
				new TextDocumentIdentifier( uri ), new Position( line, character ),
				helperExpansionDepth ) );
	}

	private static ResponseError diagramError( CompletableFuture< String > request ) {
		CompletionException completion = assertThrows( CompletionException.class, request::join );
		ResponseErrorException failure = assertInstanceOf(
				ResponseErrorException.class, completion.getCause() );
		return failure.getResponseError();
	}

}
