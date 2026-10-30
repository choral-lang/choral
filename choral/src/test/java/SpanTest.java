import choral.ast.Node;
import choral.ast.Position;
import choral.ast.Span;
import choral.compiler.Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SpanTest {

	@Test
	public void parserRecordsClassAndMethodBoundaries() {
		var unit = Parser.parseString( "class Example@( A ) {\n\tvoid run() {\n\t}\n}" );
		var declaration = unit.classes().get( 0 );
		assertSpan( declaration, 1, 0, 4, 0 );
		assertSpan( declaration.methods().get( 0 ), 2, 1, 3, 1 );
	}

	@Test
	public void parserRecordsInterfaceAndMethodBoundaries() {
		var unit = Parser.parseString( "interface Example@( A ) {\n\tvoid run();\n}" );
		var declaration = unit.interfaces().get( 0 );
		assertSpan( declaration, 1, 0, 3, 0 );
		assertSpan( declaration.methods().get( 0 ), 2, 1, 2, 11 );
	}

	@Test
	public void copyingAndResettingPositionsPreservesConsistentSpans() {
		var unit = Parser.parseString( "class Example@( A ) {\n\tvoid run() {}\n}" );
		var declaration = unit.classes().get( 0 );
		var method = declaration.methods().get( 0 );
		declaration.copyPosition( method );
		assertEquals( method.span(), declaration.span() );
		assertEquals( method.position(), declaration.position() );
		var position = new Position( null, 8, 3 );
		declaration.setPosition( position );
		assertEquals( Span.at( position ), declaration.span() );
		declaration.setSpan( null );
		assertNull( declaration.span() );
		assertNull( declaration.position() );
	}

	private static void assertSpan(
			Node node, int startLine, int startColumn,
			int endLine, int endColumn
	) {
		assertEquals( startLine, node.span().start().line() );
		assertEquals( startColumn, node.span().start().column() );
		assertEquals( endLine, node.span().end().line() );
		assertEquals( endColumn, node.span().end().column() );
		assertEquals( node.position(), node.span().start() );
	}
}
