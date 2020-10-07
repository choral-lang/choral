package Karatsuba;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Karatsuba extends AbstractBehavior< KaratsubaMessage > {

	private static final ActorSystem< KaratsubaMessage > system = ActorSystem.create( Karatsuba.create(), "KaratsubaTest" );

	public static void main( String[] args ) {
		system.tell( new KaratsubaOperation( 1241243L, 15214123L ) );
	}

	public static Behavior< KaratsubaMessage > create(){
		return Behaviors.setup( Karatsuba::new );
	}

	private Karatsuba( ActorContext< KaratsubaMessage > context ){ super( context ); }

	@Override
	public Receive< KaratsubaMessage > createReceive() {
		return newReceiveBuilder()
				.onMessage( KaratsubaOperation.class, this::onReceive )
				.onMessage( KaratsubaRequest.class, this::onReceive )
				.onMessage( KaratsubaResponse.class, this::onReceive )
				.build();
	}

	Long z0, z1, z2;
	Integer splitter;
	KaratsubaRequest requestMessage;

	private Behavior< KaratsubaMessage > onReceive( KaratsubaOperation n ) {
		System.out.println( "onStart" );
		getContext().getSelf().tell( new KaratsubaRequest( n, getContext().getSelf() , new KaratsubaResponse() ) );
		return this;
	}

	private Behavior< KaratsubaMessage > onReceive( KaratsubaRequest n ) {
		System.out.println( "onReceive" );
		Long left = n.operation().left();
		Long right = n.operation().right();
		if( left > 10 || right > 10 ){
			n.sender().tell( n.response().complete( left * right ) );
		} else {
			requestMessage = n; // we will respond asynchronously
			Double m = Math.max( Math.log10( left ), Math.log10( right ) ) + 1;
			Integer m2 = Double.valueOf( m / 2 ).intValue();
			splitter = Double.valueOf( Math.pow( 10, m2 ) ).intValue();
			Long left_h = left / splitter; Long left_l = left % splitter;
			Long right_h = right / splitter; Long right_l = right % splitter;
			getContext().spawn( Karatsuba.create(), "Z0" )
					.tell( new KaratsubaRequest(
							new KaratsubaOperation( left_l, right_l ), getContext().getSelf(), new Z0_KaratsubaResponse() )
					);
			getContext().spawn( Karatsuba.create(), "Z2" )
					.tell( new KaratsubaRequest(
							new KaratsubaOperation( left_h, right_h ), getContext().getSelf(), new Z2_KaratsubaResponse() )
					);
			getContext().spawn( Karatsuba.create(), "Z1" )
					.tell( new KaratsubaRequest(
							new KaratsubaOperation( left_l + left_h, right_l + right_h ), getContext().getSelf(), new Z2_KaratsubaResponse() )
					);
		}
		return this;
	}

	private Behavior< KaratsubaMessage > onReceive( Z0_KaratsubaResponse n ) {
		z0 = n.response();
		checkResponse();
		return this;
	}

	private Behavior< KaratsubaMessage > onReceive( Z1_KaratsubaResponse n ) {
		z1 = n.response();
		checkResponse();
		return this;
	}

	private Behavior< KaratsubaMessage > onReceive( Z2_KaratsubaResponse n ) {
		z2 = n.response();
		checkResponse();
		return this;
	}

	private void checkResponse(){
		if( z0 != null && z1 != null & z2 != null ){
			z1 = z1 - z2 - z0;
			requestMessage.sender().tell( requestMessage.response().complete( z2 * splitter * splitter + z1 * splitter + z0 ) );
		}
	}

	private Behavior< KaratsubaMessage > onReceive( KaratsubaResponse n ){
		System.out.println( "Multiplication response: " + n.response() );
		return Behaviors.stopped();
	}


}
