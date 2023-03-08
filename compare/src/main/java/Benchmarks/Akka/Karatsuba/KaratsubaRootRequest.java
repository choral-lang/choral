package Benchmarks.Akka.Karatsuba;

import java.util.concurrent.CompletableFuture;

public class KaratsubaRootRequest extends KaratsubaRequest {
	private final CompletableFuture< Long > resultFuture = new CompletableFuture<>();

	public KaratsubaRootRequest( KaratsubaOperation operation ) {
		super( operation, null, null );
	}

	public CompletableFuture< Long > resultFuture() {
		return resultFuture;
	}
}
