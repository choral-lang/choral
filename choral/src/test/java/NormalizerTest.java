import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import choral.ast.CompilationUnit;
import choral.ast.body.ClassMethodDefinition;
import choral.ast.body.ConstructorDefinition;
import choral.ast.body.VariableDeclaration;
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
		CompilationUnit normalized = normalizeResult( typedCu ).compilationUnit();
		return new PrettyPrinterVisitor().visit( normalized );
	}

	private static Normalizer.Result normalizeResult( String source ) throws IOException {
		CompilationUnit cu = Parser.parseString( source );
		TyperOptions opts = new TyperOptions( VerbosityLevel.WARNINGS ).relaxedMode();
		Collection< CompilationUnit > typed = Typer.annotate(
				List.of( cu ),
				HeaderLoader.loadStandardProfile().toList(),
				opts );
		return normalizeResult( typed.iterator().next() );
	}

	private static Normalizer.Result normalizeResult( CompilationUnit typedCu ) {
		return new Normalizer().normalize( typedCu );
	}

	private static List< VariableDeclaration > hoistsForMethod(
			Normalizer.Result result, String methodName ) {
		for( Map.Entry< Object, List< VariableDeclaration > > entry :
				result.hoistedDefinitions().entrySet() ) {
			if( entry.getKey() instanceof ClassMethodDefinition method
					&& method.signature().name().identifier().equals( methodName ) ) {
				return entry.getValue();
			}
		}
		throw new AssertionError( "No method hoists found for " + methodName );
	}

	private static List< VariableDeclaration > hoistsForConstructor(
			Normalizer.Result result, String constructorName ) {
		for( Map.Entry< Object, List< VariableDeclaration > > entry :
				result.hoistedDefinitions().entrySet() ) {
			if( entry.getKey() instanceof ConstructorDefinition constructor
					&& constructor.signature().name().identifier().equals( constructorName ) ) {
				return entry.getValue();
			}
		}
		throw new AssertionError( "No constructor hoists found for " + constructorName );
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
	public void deduplicatesRepeatedPureVariableHoists() throws IOException {
		String src =
			"""
			package test;
			class Thing@D {}
			class C@( A, B ) {
				public void takeB( Thing@B first, Thing@B second ) {}
				public void run( Thing@A y ) {
					this.takeB( y, y );
				}
			}
			""";
		String expected =
			"""
			package test;
			class Thing@D {}
			class C@( A, B ) {
				public void takeB( Thing@B first, Thing@B second ) {}
				public void run( Thing@A y ) {
					Thing@B tmp0 = y;
					this.takeB( tmp0, tmp0 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void deduplicatesRepeatedPureScopedFieldHoists() throws IOException {
		String src =
			"""
			package test;
			class Bar@D {}
			class Foo@D { public Bar@D bar; }
			class Thing@D { public Foo@D foo; }
			class C@( A, B ) {
				public void takeB( Bar@B first, Bar@B second ) {}
				public void run( Thing@A y ) {
					this.takeB( y.foo.bar, y.foo.bar );
				}
			}
			""";
		String expected =
			"""
			package test;
			class Bar@D {}
			class Foo@D { public Bar@D bar; }
			class Thing@D { public Foo@D foo; }
			class C@( A, B ) {
				public void takeB( Bar@B first, Bar@B second ) {}
				public void run( Thing@A y ) {
					Bar@B tmp0 = y.foo.bar;
					this.takeB( tmp0, tmp0 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void doesNotDeduplicateSamePureExpressionAcrossDifferentExpectedWorlds() throws IOException {
		String src =
			"""
			package test;
			class Thing@D {}
			class C@( A, B, D ) {
				public void takeBoth( Thing@B atB, Thing@D atD ) {}
				public void run( Thing@A y ) {
					this.takeBoth( y, y );
				}
			}
			""";
		String expected =
			"""
			package test;
			class Thing@D {}
			class C@( A, B, D ) {
				public void takeBoth( Thing@B atB, Thing@D atD ) {}
				public void run( Thing@A y ) {
					Thing@B tmp0 = y;
					Thing@D tmp1 = y;
					this.takeBoth( tmp0, tmp1 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void doesNotDeduplicateRepeatedImpureMethodCallHoists() throws IOException {
		String src =
			"""
			package test;
			class Bar@D {}
			class Foo@D {
				public Bar@D bar;
				public Bar@D baz() { return bar; }
			}
			class Thing@D { public Foo@D foo; }
			class C@( A, B ) {
				public void takeB( Bar@B first, Bar@B second ) {}
				public void run( Thing@A y ) {
					this.takeB( y.foo.baz(), y.foo.baz() );
				}
			}
			""";
		String expected =
			"""
			package test;
			class Bar@D {}
			class Foo@D {
				public Bar@D bar;
				public Bar@D baz() { return bar; }
			}
			class Thing@D { public Foo@D foo; }
			class C@( A, B ) {
				public void takeB( Bar@B first, Bar@B second ) {}
				public void run( Thing@A y ) {
					Bar@B tmp0 = y.foo.baz();
					Bar@B tmp1 = y.foo.baz();
					this.takeB( tmp0, tmp1 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void deduplicatesOnlyPureHoistsInMixedExpressionList() throws IOException {
		String src =
			"""
			package test;
			class Bar@D {}
			class Foo@D {
				public Bar@D bar;
				public Bar@D baz() { return bar; }
			}
			class Thing@D { public Foo@D foo; }
			class C@( A, B ) {
				public Integer@B takeB(
					Thing@B t1, Thing@B t2,
					Bar@B b1, Bar@B b2,
					Bar@B c1, Bar@B c2 ) { return 0@B; }
				public void run( Thing@A y ) {
					Integer@B x = this.takeB(
						y, y, y.foo.bar, y.foo.bar, y.foo.baz(), y.foo.baz() );
				}
			}
			""";
		String expected =
			"""
			package test;
			class Bar@D {}
			class Foo@D {
				public Bar@D bar;
				public Bar@D baz() { return bar; }
			}
			class Thing@D { public Foo@D foo; }
			class C@( A, B ) {
				public Integer@B takeB(
					Thing@B t1, Thing@B t2,
					Bar@B b1, Bar@B b2,
					Bar@B c1, Bar@B c2 ) { return 0@B; }
				public void run( Thing@A y ) {
					Thing@B tmp0 = y;
					Bar@B tmp1 = y.foo.bar;
					Bar@B tmp2 = y.foo.baz();
					Bar@B tmp3 = y.foo.baz();
					Integer@B x = this.takeB(
						tmp0, tmp0, tmp1, tmp1, tmp2, tmp3 );
				}
			}
			""";
		assertEquals( prettyPrint( expected ), normalize( src ) );
	}

	@Test
	public void doesNotReusePureHoistDeclaredOnlyInsideChildScope() throws IOException {
		String src =
			"""
			package test;
			class Thing@D {}
			class C@( A, B ) {
				public void takeB( Thing@B y ) {}
				public void run( Boolean@A cond, Thing@A y ) {
					if( cond ) { this.takeB( y ); } else { }
					this.takeB( y );
				}
			}
			""";
		String expected =
			"""
			package test;
			class Thing@D {}
			class C@( A, B ) {
				public void takeB( Thing@B y ) {}
				public void run( Boolean@A cond, Thing@A y ) {
					if( cond ) { Thing@B tmp0 = y; this.takeB( tmp0 ); } else { }
					Thing@B tmp1 = y;
					this.takeB( tmp1 );
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

	@Test
	public void recordsPureVariableHoistMetadata() throws IOException {
		String src =
			"""
			package test;
			class Thing@D {}
			class C@( A, B ) {
				public void takeB( Thing@B y ) {}
				public void run( Thing@A y ) {
					this.takeB( y );
				}
			}
			""";
		Normalizer.Result result = normalizeResult( src );
		List< VariableDeclaration > hoists = hoistsForMethod( result, "run" );
		assertEquals( 1, hoists.size() );
		assertEquals( "tmp0", hoists.get( 0 ).name().identifier() );
		assertEquals( "Thing", hoists.get( 0 ).type().name().identifier() );
		assertEquals( "B", hoists.get( 0 ).type().worldArguments().get( 0 ).name().identifier() );
		assertTrue( hoists.get( 0 ).initializer().isPresent() );
	}

	@Test
	public void recordsDeduplicatedPureHoistOnce() throws IOException {
		String src =
			"""
			package test;
			class Thing@D {}
			class C@( A, B ) {
				public void takeB( Thing@B y1, Thing@B y2 ) {}
				public void run( Thing@A y ) {
					this.takeB( y, y );
				}
			}
			""";
		Normalizer.Result result = normalizeResult( src );
		List< VariableDeclaration > hoists = hoistsForMethod( result, "run" );
		assertEquals( 1, hoists.size() );
		assertEquals( "tmp0", hoists.get( 0 ).name().identifier() );
	}

	@Test
	public void recordsRepeatedImpureHoistsSeparately() throws IOException {
		String src =
			"""
			package test;
			class Bar@D {}
			class Foo@D { public Bar@D baz() { return new Bar@D(); } }
			class Thing@D { public Foo@D foo; }
			class C@( A, B ) {
				public void takeB( Bar@B b1, Bar@B b2 ) {}
				public void run( Thing@A y ) {
					this.takeB( y.foo.baz(), y.foo.baz() );
				}
			}
			""";
		Normalizer.Result result = normalizeResult( src );
		List< VariableDeclaration > hoists = hoistsForMethod( result, "run" );
		assertEquals( 2, hoists.size() );
		assertEquals( "tmp0", hoists.get( 0 ).name().identifier() );
		assertEquals( "tmp1", hoists.get( 1 ).name().identifier() );
	}

	@Test
	public void recordsHoistsInsideChildScopesForEnclosingMethod() throws IOException {
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
		Normalizer.Result result = normalizeResult( src );
		List< VariableDeclaration > hoists = hoistsForMethod( result, "run" );
		assertEquals( 3, hoists.size() );
		assertEquals( "tmp0", hoists.get( 0 ).name().identifier() );
		assertEquals( "tmp1", hoists.get( 1 ).name().identifier() );
		assertEquals( "tmp2", hoists.get( 2 ).name().identifier() );
	}

	@Test
	public void recordsConstructorHoistMetadata() throws IOException {
		String src =
			"""
			package test;
			class C@( A, B ) {
				public C( Integer@B b ) {
					this.takeA( b );
				}
				public void takeA( Integer@A x ) {}
			}
			""";
		Normalizer.Result result = normalizeResult( src );
		List< VariableDeclaration > hoists = hoistsForConstructor( result, "C" );
		assertEquals( 1, hoists.size() );
		assertEquals( "tmp0", hoists.get( 0 ).name().identifier() );
		assertEquals( "Integer", hoists.get( 0 ).type().name().identifier() );
		assertEquals( "A", hoists.get( 0 ).type().worldArguments().get( 0 ).name().identifier() );
	}
}
