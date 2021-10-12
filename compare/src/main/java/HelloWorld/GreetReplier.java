package HelloWorld;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class GreetReplier extends AbstractBehavior< GreetMessage > {

	private final int max;
	private int greetingCounter;

	public GreetReplier( ActorContext< GreetMessage > context, int max ) {
		super( context );
		this.max = max;
	}

	public static Behavior< GreetMessage > create( int max ){
		return Behaviors.setup( context -> new GreetReplier( context, max ) );
	}

	@Override
	public Receive< GreetMessage > createReceive() {
		return newReceiveBuilder().onMessage( GreetMessage.class, this::onGreeted ).build();
	}

	private Behavior< GreetMessage > onGreeted( GreetMessage message ) {
		greetingCounter++;
		System.out.println( "Greeting n. " + greetingCounter + " to " + message.name );
		if( greetingCounter == max ){
			return Behaviors.stopped();
		} else {
			message.actorReference.tell( new GreetMessage( message.name, getContext().getSelf() ) );
			return this;
		}
	}


}
