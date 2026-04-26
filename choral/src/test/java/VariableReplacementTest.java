import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import choral.ast.CompilationUnit;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.compiler.HeaderLoader;
import choral.compiler.Parser;
import choral.compiler.Typer;
import choral.compiler.TyperOptions;
import choral.compiler.moveMeant.VariableReplacement;
import choral.exceptions.CommunicationInferenceException;
import choral.utils.VerbosityLevel;

public class VariableReplacementTest {

	private static String inferComms( String source ) throws IOException {
		CompilationUnit inferred = inferCommsCu( source );
		return new PrettyPrinterVisitor().visit( inferred );
	}

	private static CompilationUnit inferCommsCu( String source ) throws IOException {
		CompilationUnit cu = Parser.parseString( source );
		TyperOptions opts = new TyperOptions( VerbosityLevel.WARNINGS ).relaxedMode();
		Collection< CompilationUnit > typed = Typer.annotate(
				List.of( cu ),
				HeaderLoader.loadStandardProfile().toList(),
				opts );
		return new VariableReplacement().inferComms( typed.iterator().next() );
	}

	private static String prettyPrint( String source ) {
		return new PrettyPrinterVisitor().visit( Parser.parseString( source ) );
	}

	@Test
	public void wrapsLiteralHoistWithCommunication() throws IOException {
		String src =
			"""
			package test;
			import choral.channels.SymChannel;
			class C@( A, B ) {
				public void takeB( Integer@B x ) {}
				public void run( SymChannel@( A, B )< Object > ch_AB ) {
					this.takeB( 5@A );
				}
			}
			""";
		String expected =
			"""
			package test;
			import choral.channels.SymChannel;
			class C@( A, B ) {
				public void takeB( Integer@B x ) {}
				public void run( SymChannel@( A, B )< Object > ch_AB ) {
					Integer@B msg0 = ch_AB.< Integer >com( 5@A );
					this.takeB( msg0 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), inferComms( src ) );
	}

	@Test
	public void wrapsVariableScopedFieldAndImpureMethodHoists() throws IOException {
		String src =
			"""
			package test;
			import choral.channels.SymChannel;
			class Foo@D {
				public Integer@D value;
				public Integer@D get() { return value; }
			}
			class C@( A, B ) {
				public void takeB( Integer@B x, Integer@B y, Integer@B z ) {}
				public void run( SymChannel@( A, B )< Object > ch_AB, Integer@A a, Foo@A foo ) {
					this.takeB( a, foo.value, foo.get() );
				}
			}
			""";
		String expected =
			"""
			package test;
			import choral.channels.SymChannel;
			class Foo@D {
				public Integer@D value;
				public Integer@D get() { return value; }
			}
			class C@( A, B ) {
				public void takeB( Integer@B x, Integer@B y, Integer@B z ) {}
				public void run( SymChannel@( A, B )< Object > ch_AB, Integer@A a, Foo@A foo ) {
					Integer@B msg0 = ch_AB.< Integer >com( a );
					Integer@B msg1 = ch_AB.< Integer >com( foo.value );
					Integer@B msg2 = ch_AB.< Integer >com( foo.get() );
					this.takeB( msg0, msg1, msg2 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), inferComms( src ) );
	}

	@Test
	public void communicatesDeduplicatedPureHoistOnce() throws IOException {
		String src =
			"""
			package test;
			import choral.channels.SymChannel;
			class Thing@D {}
			class C@( A, B ) {
				public void takeB( Thing@B x, Thing@B y ) {}
				public void run( SymChannel@( A, B )< Object > ch_AB, Thing@A thing ) {
					this.takeB( thing, thing );
				}
			}
			""";
		String expected =
			"""
			package test;
			import choral.channels.SymChannel;
			class Thing@D {}
			class C@( A, B ) {
				public void takeB( Thing@B x, Thing@B y ) {}
				public void run( SymChannel@( A, B )< Object > ch_AB, Thing@A thing ) {
					Thing@B msg0 = ch_AB.< Thing >com( thing );
					this.takeB( msg0, msg0 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), inferComms( src ) );
	}

	@Test
	public void wrapsConstructorHoistsWithConstructorScopedChannel() throws IOException {
		String src =
			"""
			package test;
			import choral.channels.SymChannel;
			class C@( A, B ) {
				public C( SymChannel@( A, B )< Object > ch_AB, Integer@A a ) {
					Integer@B x = a;
				}
			}
			""";
		String expected =
			"""
			package test;
			import choral.channels.SymChannel;
			class C@( A, B ) {
				public C( SymChannel@( A, B )< Object > ch_AB, Integer@A a ) {
					Integer@B msg0 = ch_AB.< Integer >com( a );
					Integer@B x = msg0;
				}
			}
			""";
		assertEquals( prettyPrint( expected ), inferComms( src ) );
	}

	@Test
	public void missingChannelThrowsCommunicationInferenceException() {
		String src =
			"""
			package test;
			class C@( A, B ) {
				public void run( Integer@A a ) {
					Integer@B x = a;
				}
			}
			""";
		assertThrows( CommunicationInferenceException.class, () -> inferCommsCu( src ) );
	}
}
