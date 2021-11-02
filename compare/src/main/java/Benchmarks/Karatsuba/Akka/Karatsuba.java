package Benchmarks.Karatsuba.Akka;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Karatsuba extends AbstractBehavior< KaratsubaMessage > {

	public static Behavior< KaratsubaMessage > create() {
		return Behaviors.setup( Karatsuba::new );
	}

	private Karatsuba( ActorContext< KaratsubaMessage > context ) {
		super( context );
	}

	@Override
	public Receive< KaratsubaMessage > createReceive() {
		return newReceiveBuilder()
//				.onMessage( KaratsubaOperation.class, this::onReceive )
//				.onMessage( KaratsubaRootRequest.class, this::onReceive )
				.onMessage( KaratsubaRequest.class, this::onReceive )
				.onMessage( KaratsubaResponse.class, this::onReceive )
				.build();
	}

	Long z0, z1, z2;
	Integer splitter;
	KaratsubaRequest requestMessage;

//	private Behavior< KaratsubaMessage > onReceive( KaratsubaOperation n ) {
////		System.out.println( "onStart" );
//		getContext().getSelf().tell(
//				new KaratsubaRequest( n, getContext().getSelf(), new Z0_KaratsubaResponse() ) );
//		return this;
//	}

	private Behavior< KaratsubaMessage > onReceive( KaratsubaRequest n ) {
		requestMessage = n;
		Long left = n.operation().left();
		Long right = n.operation().right();
		if( left < 10 || right < 10 ) {
			sendResponse( left * right );
		} else {
			Double m = Math.max( Math.log10( left ), Math.log10( right ) ) + 1;
			Integer m2 = Double.valueOf( m / 2 ).intValue();
			splitter = Double.valueOf( Math.pow( 10, m2 ) ).intValue();
			Long left_h = left / splitter;
			Long left_l = left % splitter;
			Long right_h = right / splitter;
			Long right_l = right % splitter;
			getContext().spawn( Karatsuba.create(), "Z0" )
					.tell( new KaratsubaRequest(
							new KaratsubaOperation( left_l, right_l ), getContext().getSelf(),
							new Z0_KaratsubaResponse() )
					);
			getContext().spawn( Karatsuba.create(), "Z2" )
					.tell( new KaratsubaRequest(
							new KaratsubaOperation( left_h, right_h ), getContext().getSelf(),
							new Z2_KaratsubaResponse() )
					);
			getContext().spawn( Karatsuba.create(), "Z1" )
					.tell( new KaratsubaRequest(
							new KaratsubaOperation( left_l + left_h, right_l + right_h ),
							getContext().getSelf(), new Z1_KaratsubaResponse() )
					);
		}
		return this;
	}

	private Behavior< KaratsubaMessage > onReceive( Z0_KaratsubaResponse n ) {
		z0 = n.result();
		checkResponse();
		return this;
	}

	private Behavior< KaratsubaMessage > onReceive( Z1_KaratsubaResponse n ) {
		z1 = n.result();
		checkResponse();
		return this;
	}

	private Behavior< KaratsubaMessage > onReceive( Z2_KaratsubaResponse n ) {
		z2 = n.result();
		checkResponse();
		return this;
	}

	private void checkResponse() {
//		System.out.println( "checkResponse" );
		if( z0 != null && z1 != null & z2 != null ) {
//			System.out.println( "checkResponse 2" );
			z1 = z1 - z2 - z0;
			sendResponse( z2 * splitter * splitter + z1 * splitter + z0 );
		}
	}

	private void sendResponse( Long result ) {
		if( requestMessage instanceof KaratsubaRootRequest rootRequest ) {
			rootRequest.resultFuture().complete( result );
		} else {
			requestMessage.sender().tell( requestMessage.response().complete( result ) );
		}
	}

	private Behavior< KaratsubaMessage > onReceive( KaratsubaResponse n ) {
//		System.out.println( "Multiplication response: " + n.getClass() );
		if( n instanceof Z0_KaratsubaResponse resp ) {
			return onReceive( resp );
		} else if( n instanceof Z1_KaratsubaResponse resp ) {
			return onReceive( resp );
		} else if( n instanceof Z2_KaratsubaResponse resp ) {
			return onReceive( resp );
		} else {
			return Behaviors.stopped();
		}
	}
}
