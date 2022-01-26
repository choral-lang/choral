package choral.examples.BiPair;

import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "B", name = "BiPair" )
public class BiPair_B< L, R > {
	private R right;

	public BiPair_B( Unit left, R right ) {
		this( right );
	}

	public BiPair_B( R right ) {
		this.right = right;
	}

	public Unit left() {
		return Unit.id;
	}

	public R right() {
		return this.right;
	}

}
