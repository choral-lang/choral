import choral.ast.CompilationUnit;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.compiler.HeaderLoader;
import choral.compiler.Parser;
import choral.compiler.Typer;
import choral.compiler.TyperOptions;
import choral.exceptions.AstPositionedException;
import choral.utils.VerbosityLevel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FinalLocalVariableTest {

	private static CompilationUnit typecheck( String source ) throws IOException {
		CompilationUnit cu = Parser.parseString( source );
		return Typer.annotate(
				List.of( cu ),
				HeaderLoader.loadStandardProfile().toList(),
				new TyperOptions( VerbosityLevel.WARNINGS ) ).iterator().next();
	}

	@Test
	public void parsesAndPrintsFinalLocalVariable() {
		String source =
				"""
				package test;
				class C@( A ) {
					public void run() {
						final Integer@A x = 5@A;
					}
				}
				""";

		assertTrue(
				new PrettyPrinterVisitor().visit( Parser.parseString( source ) )
						.contains( "final Integer@( A ) x = 5@A;" ) );
	}

	@Test
	public void allowsFinalLocalVariableInitializer() throws IOException {
		typecheck(
				"""
				package test;
				class C@( A ) {
					public void run() {
						final Integer@A x = 5@A;
						System@A.out.println( x );
					}
				}
				""" );
	}

	@Test
	public void rejectsAssignmentToFinalLocalVariable() {
		AstPositionedException thrown = assertThrows( AstPositionedException.class, () ->
				typecheck(
						"""
						package test;
						class C@( A ) {
							public void run() {
								final Integer@A x = 5@A;
								x += 1@A;
							}
						}
						""" ) );

		assertEquals( "cannot assign a value to final variable 'x'", thrown.getInnerMessage() );
	}

	@Test
	public void allowsAssignmentToNonFinalLocalVariable() throws IOException {
		typecheck(
				"""
				package test;
				class C@( A ) {
					public void run() {
						Integer@A x = 5@A;
						x += 1@A;
					}
				}
				""" );
	}
}
