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
				"package test;\n" +
				"class Empty@( A ) { }\n";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}

	@Test
	public void sameWorldAssignment() throws IOException {
		String src =
				"package test;\n" +
				"class SameWorld@( A ) {\n" +
				"\tpublic void m() {\n" +
				"\t\tInteger@A x = 1@A;\n" +
				"\t\tInteger@A y = x + 1@A;\n" +
				"\t}\n" +
				"}\n";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}

	@Test
	public void sameWorldMethodCall() throws IOException {
		String src =
				"package test;\n" +
				"class C@( A ) {\n" +
				"\tpublic Integer@A m( Integer@A x ) { return x; }\n" +
				"\tpublic void run() {\n" +
				"\t\tInteger@A y = this.m( 1@A );\n" +
				"\t}\n" +
				"}\n";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}

	@Test
	public void sameWorldIfReturn() throws IOException {
		String src =
				"package test;\n" +
				"class C@( A ) {\n" +
				"\tpublic Integer@A m( Boolean@A b ) {\n" +
				"\t\tif( b ) { return 1@A; } else { return 2@A; }\n" +
				"\t}\n" +
				"}\n";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}

	@Test
	public void sameWorldBlockAndNot() throws IOException {
		String src =
				"package test;\n" +
				"class C@( A ) {\n" +
				"\tpublic Boolean@A m( Boolean@A b ) {\n" +
				"\t\t{ Boolean@A c = !b; return c; }\n" +
				"\t}\n" +
				"}\n";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}

	@Test
	public void sameWorldEnclosed() throws IOException {
		String src =
				"package test;\n" +
				"class C@( A ) {\n" +
				"\tpublic Integer@A m() {\n" +
				"\t\tInteger@A x = ( 1@A + 2@A );\n" +
				"\t\treturn x;\n" +
				"\t}\n" +
				"}\n";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}

	@Test
	public void hoistsCrossWorldMethodArg() throws IOException {
		String src =
				"package test;\n" +
				"class C@( A, B ) {\n" +
				"\tpublic Integer@A take( Integer@A x ) { return x; }\n" +
				"\tpublic void run( Integer@B b ) {\n" +
				"\t\tthis.take( b );\n" +
				"\t}\n" +
				"}\n";
		String expected =
				"package test;\n" +
				"class C@( A, B ) {\n" +
				"\tpublic Integer@A take( Integer@A x ) { return x; }\n" +
				"\tpublic void run( Integer@B b ) {\n" +
				"\t\tInteger@B tmp0 = b;\n" +
				"\t\tthis.take( tmp0 );\n" +
				"\t}\n" +
				"}\n";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void hoistsTwoCrossWorldArgsInOrder() throws IOException {
		String src =
				"package test;\n" +
				"class C@( A, B, D ) {\n" +
				"\tpublic Integer@A take( Integer@A x, Integer@A y ) { return x; }\n" +
				"\tpublic void run( Integer@B b, Integer@D d ) {\n" +
				"\t\tthis.take( b, d );\n" +
				"\t}\n" +
				"}\n";
		String expected =
				"package test;\n" +
				"class C@( A, B, D ) {\n" +
				"\tpublic Integer@A take( Integer@A x, Integer@A y ) { return x; }\n" +
				"\tpublic void run( Integer@B b, Integer@D d ) {\n" +
				"\t\tInteger@B tmp0 = b;\n" +
				"\t\tInteger@D tmp1 = d;\n" +
				"\t\tthis.take( tmp0, tmp1 );\n" +
				"\t}\n" +
				"}\n";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void sameWorldArgsNotHoisted() throws IOException {
		String src =
				"package test;\n" +
				"class C@( A, B ) {\n" +
				"\tpublic Integer@A take( Integer@A x ) { return x; }\n" +
				"\tpublic void run( Integer@A a ) {\n" +
				"\t\tthis.take( a );\n" +
				"\t}\n" +
				"}\n";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}
}
