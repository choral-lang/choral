package Benchmarks.Karatsuba.Akka;

public class KaratsubaOperation implements KaratsubaMessage {
	private final Long left, right;

	public KaratsubaOperation( Long left, Long right ) {
		this.left = left;
		this.right = right;
	}

	public Long left() {
		return left;
	}

	public Long right() {
		return right;
	}
}
