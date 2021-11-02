package Karatsuba;

import akka.actor.typed.ActorRef;

public interface KaratsubaMessage {}

class KaratsubaRequest implements KaratsubaMessage {
	private final KaratsubaOperation operation;
	private final ActorRef< KaratsubaMessage > sender;
	private final KaratsubaResponse response; // this defined the "format" of the response (either complete, Z0, Z1 or Z2)

	public KaratsubaRequest( KaratsubaOperation operation, ActorRef< KaratsubaMessage > sender, KaratsubaResponse response ) {
		this.operation = operation;
		this.sender = sender;
		this.response = response;
	}
	public KaratsubaOperation operation() { return this.operation; }
	public ActorRef< KaratsubaMessage > sender() { return this.sender; }
	public KaratsubaResponse response() { return this.response; }
}

class KaratsubaOperation implements KaratsubaMessage {
	private final Long left, right;
	public KaratsubaOperation( Long left, Long right ) {
		this.left = left;
		this.right = right;
	}
	public Long left() { return left; }
	public Long right() { return right;	}
}

// complete response
class KaratsubaResponse implements KaratsubaMessage {
	private Long response;
	public KaratsubaResponse() {}
	public KaratsubaResponse complete( Long response ){
		this.response = response;
		return this;
	}
	public Long response() { return response; }
}

// partial responses
class Z0_KaratsubaResponse extends KaratsubaResponse {}
class Z1_KaratsubaResponse extends KaratsubaResponse {}
class Z2_KaratsubaResponse extends KaratsubaResponse {}



