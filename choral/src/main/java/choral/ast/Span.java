package choral.ast;

import java.util.Objects;

/** The inclusive source span between two positions. */
public record Span(Position start, Position end) {
	public Span {
		Objects.requireNonNull( start );
		Objects.requireNonNull( end );
	}

	/** Creates a zero-length range at a single source position. */
	public static Span at( Position position ) {
		return new Span( position, position );
	}
}
