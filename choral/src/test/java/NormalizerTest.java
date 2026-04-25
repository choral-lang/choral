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
					Integer@A tmp0 = b;
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
					Integer@A tmp0 = b;
					Integer@A tmp1 = d;
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

	@Test
	public void hoistsCrossWorldBinaryRightOperand() throws IOException {
		String src =
			"""
			package test;
			class C@( A, B ) {
				public void run( Integer@A a, Integer@B b ) {
					Integer@A z = a + b;
				}
			}
			""";
		String expected =
			"""
			package test;
			class C@( A, B ) {
				public void run( Integer@A a, Integer@B b ) {
					Integer@A tmp0 = b;
					Integer@A z = a + tmp0;
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void hoistsCrossWorldAssignRhs() throws IOException {
		String src =
			"""
			package test;
			class C@( A, B ) {
				public void run( Integer@A a, Integer@B b ) {
					a = b;
				}
			}
			""";
		String expected =
			"""
			package test;
			class C@( A, B ) {
				public void run( Integer@A a, Integer@B b ) {
					Integer@A tmp0 = b;
					a = tmp0;
				}
			}
		""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void hoistsNestedAlternatingWorldMethodCallsInsideOut() throws IOException {
		String src =
			"""
			package test;
			class C@( A, B ) {
				public Integer@A takeA( Integer@A x ) { return x; }
				public Integer@B makeB( Integer@B x ) { return x; }
				public void run( Integer@A a ) {
					this.takeA( this.makeB( a ) );
				}
			}
			""";
		String expected =
			"""
			package test;
			class C@( A, B ) {
				public Integer@A takeA( Integer@A x ) { return x; }
				public Integer@B makeB( Integer@B x ) { return x; }
				public void run( Integer@A a ) {
					Integer@B tmp0 = a;
					Integer@A tmp1 = this.makeB( tmp0 );
					this.takeA( tmp1 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void hoistsThreeLevelAlternatingWorldMethodCallsInsideOut() throws IOException {
		String src =
			"""
			package test;
			class C@( A, B ) {
				public Integer@A takeA( Integer@A x ) { return x; }
				public Integer@B makeB( Integer@B x ) { return x; }
				public void run( Integer@B b ) {
					this.takeA( this.makeB( this.takeA( b ) ) );
				}
			}
			""";
		String expected =
			"""
			package test;
			class C@( A, B ) {
				public Integer@A takeA( Integer@A x ) { return x; }
				public Integer@B makeB( Integer@B x ) { return x; }
				public void run( Integer@B b ) {
					Integer@A tmp0 = b;
					Integer@B tmp1 = this.takeA( tmp0 );
					Integer@A tmp2 = this.makeB( tmp1 );
					this.takeA( tmp2 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void hoistsNotExpressionAsWhole() throws IOException {
		String src =
			"""
			package test;
			class C@( A, B ) {
				public void take( Boolean@A x ) {}
				public void run( Boolean@B b ) {
					this.take( !b );
				}
			}
			""";
		String expected =
			"""
			package test;
			class C@( A, B ) {
				public void take( Boolean@A x ) {}
				public void run( Boolean@B b ) {
					Boolean@A tmp0 = !b;
					this.take( tmp0 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void hoistsEnclosedExpressionAsWhole() throws IOException {
		String src =
			"""
			package test;
			class C@( A, B ) {
				public void take( Integer@A x ) {}
				public void run( Integer@B b ) {
					this.take( ( b ) );
				}
			}
			""";
		String expected =
			"""
			package test;
			class C@( A, B ) {
				public void take( Integer@A x ) {}
				public void run( Integer@B b ) {
					Integer@A tmp0 = ( b );
					this.take( tmp0 );
				}
			}
		""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void hoistsThisFieldAccessAsWhole() throws IOException {
		String src =
			"""
			package test;
			class C@( A, B ) {
				public Integer@A x;
				public void take( Integer@B x ) {}
				public void run() {
					this.take( this.x );
				}
			}
			""";
		String expected =
			"""
			package test;
			class C@( A, B ) {
				public Integer@A x;
				public void take( Integer@B x ) {}
				public void run() {
					Integer@B tmp0 = this.x;
					this.take( tmp0 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void hoistsSuperFieldAccessAsWhole() throws IOException {
		String src =
			"""
			package test;
			class Base@( D, E ) {
				public Integer@D x;
			}
			class C@( A, B ) extends Base@( A, B ) {
				public void take( Integer@B x ) {}
				public void run() {
					this.take( super.x );
				}
			}
			""";
		String expected =
			"""
			package test;
			class Base@( D, E ) {
				public Integer@D x;
			}
			class C@( A, B ) extends Base@( A, B ) {
				public void take( Integer@B x ) {}
				public void run() {
					Integer@B tmp0 = super.x;
					this.take( tmp0 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void hoistsScopedFieldAccess() throws IOException {
		String src =
			"""
			package test;
			class Pair@D {
				public Integer@D x;
				public Pair( Integer@D x ) { this.x = x; }
			}
			class C@( A, B ) {
				public void take( Integer@A x ) {}
				public void run( Pair@B p ) {
					this.take( p.x );
				}
			}
			""";
		String expected =
			"""
			package test;
			class Pair@D {
				public Integer@D x;
				public Pair( Integer@D x ) { this.x = x; }
			}
			class C@( A, B ) {
				public void take( Integer@A x ) {}
				public void run( Pair@B p ) {
					Integer@A tmp0 = p.x;
					this.take( tmp0 );
				}
			}
		""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void sameWorldDeepScopedFieldAccessNotHoisted() throws IOException {
		String src =
			"""
			package test;
			class Leaf@D {
				public Integer@D baz;
				public Leaf( Integer@D baz ) { this.baz = baz; }
			}
			class Middle@D {
				public Leaf@D bar;
				public Middle( Leaf@D bar ) { this.bar = bar; }
			}
			class Foo@D {
				public Middle@D bar;
				public Foo( Middle@D bar ) { this.bar = bar; }
			}
			class C@( A ) {
				public void take( Integer@A x ) {}
				public void run( Foo@A foo ) {
					this.take( foo.bar.bar.baz );
				}
			}
			""";
		assertEquals( prettyPrint( src ), normalize( src ) );
	}

	@Test
	public void hoistsDeepScopedFieldAccessAsWhole() throws IOException {
		String src =
			"""
			package test;
			class Leaf@D {
				public Integer@D baz;
				public Leaf( Integer@D baz ) { this.baz = baz; }
			}
			class Middle@D {
				public Leaf@D bar;
				public Middle( Leaf@D bar ) { this.bar = bar; }
			}
			class Foo@D {
				public Middle@D bar;
				public Foo( Middle@D bar ) { this.bar = bar; }
			}
			class C@( A, B ) {
				public void take( Integer@A x ) {}
				public void run( Foo@B foo ) {
					this.take( foo.bar.bar.baz );
				}
			}
			""";
		String expected =
			"""
			package test;
			class Leaf@D {
				public Integer@D baz;
				public Leaf( Integer@D baz ) { this.baz = baz; }
			}
			class Middle@D {
				public Leaf@D bar;
				public Middle( Leaf@D bar ) { this.bar = bar; }
			}
			class Foo@D {
				public Middle@D bar;
				public Foo( Middle@D bar ) { this.bar = bar; }
			}
			class C@( A, B ) {
				public void take( Integer@A x ) {}
				public void run( Foo@B foo ) {
					Integer@A tmp0 = foo.bar.bar.baz;
					this.take( tmp0 );
				}
			}
		""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void hoistsStaticCallArgumentAndWholeStaticCall() throws IOException {
		String src =
			"""
			package test;
			class C@( A, B ) {
				public void run() {
					Integer@A x = Integer@B.valueOf( 42@A );
				}
			}
			""";
		String expected =
			"""
			package test;
			class C@( A, B ) {
				public void run() {
					Integer@B tmp0 = 42@A;
					Integer@A tmp1 = Integer@B.valueOf( tmp0 );
					Integer@A x = tmp1;
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void hoistsEnumCaseInstantiation() throws IOException {
		String src =
			"""
			package test;
			enum Choice@R { YES, NO }
			class C@( A, B ) {
				public void take( Choice@A choice ) {}
				public void run() {
					this.take( Choice@B.YES );
				}
			}
			""";
		String expected =
			"""
			package test;
			enum Choice@R { YES, NO }
			class C@( A, B ) {
				public void take( Choice@A choice ) {}
				public void run() {
					Choice@A tmp0 = Choice@B.YES;
					this.take( tmp0 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void hoistsCrossWorldLiteralsAndNull() throws IOException {
		String src =
			"""
			package test;
			class Box@D {}
			class C@( A, B ) {
				public void take( Integer@A i, Boolean@A b, String@A s, Box@A box ) {}
				public void run() {
					this.take( 1@B, true@B, "x"@B, null@B );
				}
			}
			""";
		String expected =
			"""
			package test;
			class Box@D {}
			class C@( A, B ) {
				public void take( Integer@A i, Boolean@A b, String@A s, Box@A box ) {}
				public void run() {
					Integer@A tmp0 = 1@B;
					Boolean@A tmp1 = true@B;
					String@A tmp2 = "x"@B;
					Box@A tmp3 = null@B;
					this.take( tmp0, tmp1, tmp2, tmp3 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void normalizesHoistsInsideScopedExpressionScope() throws IOException {
		String src =
			"""
			package test;
			class Box@D {
				public Integer@D value;
				public Box( Integer@D value ) { this.value = value; }
			}
			class C@( A, B ) {
				public Box@A getBox( Integer@A value ) { return new Box@A( value ); }
				public void take( Integer@A value ) {}
				public void run( Integer@B b ) {
					this.take( this.getBox( b ).value );
				}
			}
			""";
		String expected =
			"""
			package test;
			class Box@D {
				public Integer@D value;
				public Box( Integer@D value ) { this.value = value; }
			}
			class C@( A, B ) {
				public Box@A getBox( Integer@A value ) { return new Box@A( value ); }
				public void take( Integer@A value ) {}
				public void run( Integer@B b ) {
					Integer@A tmp0 = b;
					this.take( this.getBox( tmp0 ).value );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void normalizesSwitchGuardAndCaseBodies() throws IOException {
		String src =
			"""
			package test;
			class C@( A, B ) {
				public void run( Integer@A a, Integer@B b ) {
					switch( b + a ) {
						case 0@B -> { Integer@A x = b; }
						default -> { Integer@A y = b; }
					}
				}
			}
			""";
		String expected =
			"""
			package test;
			class C@( A, B ) {
				public void run( Integer@A a, Integer@B b ) {
					Integer@B tmp0 = a;
					switch( b + tmp0 ) {
						case 0@B -> { Integer@A tmp1 = b; Integer@A x = tmp1; }
						default -> { Integer@A tmp2 = b; Integer@A y = tmp2; }
					}
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}
}
