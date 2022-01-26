package Benchmarks.Karatsuba.Akka;

import akka.actor.typed.ActorRef;

public interface KaratsubaMessage {
}

class KaratsubaRequest implements KaratsubaMessage {
	private final KaratsubaOperation operation;
	private final ActorRef< KaratsubaMessage > sender;
	private final KaratsubaResponse response; // this defined the "format" of the response (either complete, Z0, Z1 or Z2)

	public KaratsubaRequest(
			KaratsubaOperation operation, ActorRef< KaratsubaMessage > sender,
			KaratsubaResponse response
	) {
		this.operation = operation;
		this.sender = sender;
		this.response = response;
	}

	public KaratsubaOperation operation() {
		return this.operation;
	}

	public ActorRef< KaratsubaMessage > sender() {
		return this.sender;
	}

	public KaratsubaResponse response() {
		return this.response;
	}
}

// complete response
abstract class KaratsubaResponse implements KaratsubaMessage {
	protected Long result;

	public KaratsubaResponse() {
	}

	public abstract KaratsubaResponse complete( Long result );

	public Long result() {
		return result;
	}
}

// partial responses
class Z0_KaratsubaResponse extends KaratsubaResponse {
	public Z0_KaratsubaResponse complete( Long result ) {
		this.result = result;
		return this;
	}
}

class Z1_KaratsubaResponse extends KaratsubaResponse {
	public Z1_KaratsubaResponse complete( Long result ) {
		this.result = result;
		return this;
	}
}

class Z2_KaratsubaResponse extends KaratsubaResponse {
	public Z2_KaratsubaResponse complete( Long result ) {
		this.result = result;
		return this;
	}
}



