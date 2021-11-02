package MergeSort;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;

public class SortingSystem extends AbstractBehavior< Message > {

	private static final ActorSystem< Message > system = ActorSystem.create( SortingSystem.create(), "SortingSystem" );

	public static void main( String[] args ) {
		System.out.println( "Launching sort" );
		system.tell( new SortMessage( List.of( 5, 12, 34, 23, 1, 234, 7 ), system ) );
	}

	public static Behavior< Message > create(){
		return Behaviors.setup( SortingSystem::new );
	}

	private final ActorRef< Message > location_A;

	private SortingSystem( ActorContext< Message > context ){
		super( context );
		location_A = context.spawn( SortingLocation.create(), "Location_A" );
	}

	@Override
	public Receive< Message > createReceive() {
		return newReceiveBuilder()
				.onMessage( SortMessage.class, this::onStart )
				.onMessage( MergeMessage.class, this::onResponse )
				.build();
	}

	private Behavior< Message > onStart( SortMessage m ) {
		location_A.tell( m );
		return this;
	}

	private Behavior< Message > onResponse( MergeMessage m ){
		System.out.println( "Received response: " + m.list() );
		return Behaviors.stopped();
	}
}
