package Benchmarks.Akka.DistAuth.DistAuthUtils;

public class BiPair_B< L, R > {
	private R right;

	public BiPair_B( Void left, R right ) {
		this( right );
	}

	public BiPair_B( R right ) {
		this.right = right;
	}

	public Void left() {
		return null;
	}

	public R right() {
		return this.right;
	}

}
