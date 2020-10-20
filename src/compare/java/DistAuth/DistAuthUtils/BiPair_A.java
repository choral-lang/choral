package DistAuth.DistAuthUtils;

public class BiPair_A< L, R > {
	private L left;

	public BiPair_A( L left, Void right ) {
		this( left );
	}

	public BiPair_A( L left ) {
		this.left = left;
	}

	public L left() {
		return this.left;
	}

	public Void right() {
		return null;
	}

}
