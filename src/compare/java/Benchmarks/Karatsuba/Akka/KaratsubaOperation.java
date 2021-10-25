package Benchmarks.Karatsuba.Akka;

import java.util.concurrent.CompletableFuture;

public class KaratsubaOperation implements KaratsubaMessage {
	private final Long left, right;
	private final CompletableFuture< Long > result;

	public KaratsubaOperation(
			Long left, Long right, CompletableFuture< Long > result
	) {
		this.left = left;
		this.right = right;
		this.result = result;
	}

	public Long left() {
		return left;
	}

	public Long right() {
		return right;
	}

	public CompletableFuture< Long > result() {
		return result;
	}
}
