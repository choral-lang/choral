import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import choral.ast.CompilationUnit;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.compiler.HeaderLoader;
import choral.compiler.Parser;
import choral.compiler.Typer;
import choral.compiler.TyperOptions;
import choral.compiler.moveMeant.Normalizer;
import choral.utils.VerbosityLevel;

public class NormalizerTest {

	/**
	 * Blackbox harness: parse, typecheck in relaxed mode, run Normalizer,
	 * pretty-print the result.
	 */
	private static String normalize( String source ) throws IOException {
		CompilationUnit cu = Parser.parseString( source );
		TyperOptions opts = new TyperOptions( VerbosityLevel.WARNINGS ).relaxedMode();
		Collection< CompilationUnit > typed = Typer.annotate(
				List.of( cu ),
				HeaderLoader.loadStandardProfile().toList(),
				opts );
		CompilationUnit typedCu = typed.iterator().next();
		CompilationUnit normalized = new Normalizer().normalize( typedCu );
		return new PrettyPrinterVisitor().visit( normalized );
	}

	/** Parse and pretty-print, for golden comparisons in identity cases. */
	private static String prettyPrint( String source ) {
		CompilationUnit cu = Parser.parseString( source );
		return new PrettyPrinterVisitor().visit( cu );
	}

	@Test
	public void emptyClass() throws IOException {
		String src =
			"""
			package test;
			class Empty@( A ) { }
			""";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}

	@Test
	public void sameWorldAssignment() throws IOException {
		String src =
			"""
			package test;
			class SameWorld@( A ) {
				public void m() {
					Integer@A x = 1@A;
					Integer@A y = x + 1@A;
				}
			}
			""";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}

	@Test
	public void sameWorldMethodCall() throws IOException {
		String src =
			"""
			package test;
			class C@( A ) {
				public Integer@A m( Integer@A x ) { return x; }
				public void run() {
					Integer@A y = this.m( 1@A );
				}
			}
			""";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}

	@Test
	public void sameWorldIfReturn() throws IOException {
		String src =
			"""
			package test;
			class C@( A ) {
				public Integer@A m( Boolean@A b ) {
					if( b ) { return 1@A; } else { return 2@A; }
				}
			}
			""";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}

	@Test
	public void sameWorldBlockAndNot() throws IOException {
		String src =
			"""
			package test;
			class C@( A ) {
				public Boolean@A m( Boolean@A b ) {
					{ Boolean@A c = !b; return c; }
				}
			}
			""";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}

	@Test
	public void sameWorldEnclosed() throws IOException {
		String src =
			"""
			package test;
			class C@( A ) {
				public Integer@A m() {
					Integer@A x = ( 1@A + 2@A );
					return x;
				}
			}
			""";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}

	@Test
	public void hoistsCrossWorldMethodArg() throws IOException {
		String src =
			"""
			package test;
			class C@( A, B ) {
				public Integer@A take( Integer@A x ) { return x; }
				public void run( Integer@B b ) {
					this.take( b );
				}
			}
			""";
		String expected =
			"""
			package test;
			class C@( A, B ) {
				public Integer@A take( Integer@A x ) { return x; }
				public void run( Integer@B b ) {
					Integer@B tmp0 = b;
					this.take( tmp0 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void hoistsTwoCrossWorldArgsInOrder() throws IOException {
		String src =
			"""
			package test;
			class C@( A, B, D ) {
				public Integer@A take( Integer@A x, Integer@A y ) { return x; }
				public void run( Integer@B b, Integer@D d ) {
					this.take( b, d );
				}
			}
			""";
		String expected =
			"""
			package test;
			class C@( A, B, D ) {
				public Integer@A take( Integer@A x, Integer@A y ) { return x; }
				public void run( Integer@B b, Integer@D d ) {
					Integer@B tmp0 = b;
					Integer@D tmp1 = d;
					this.take( tmp0, tmp1 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void sameWorldArgsNotHoisted() throws IOException {
		String src =
			"""
			package test;
			class C@( A, B ) {
				public Integer@A take( Integer@A x ) { return x; }
				public void run( Integer@A a ) {
					this.take( a );
				}
			}
			""";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}
}
