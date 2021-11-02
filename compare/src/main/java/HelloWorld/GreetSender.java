package HelloWorld;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class GreetSender extends AbstractBehavior< GreetMessage > {

	public static Behavior< GreetMessage > create(){
		return Behaviors.setup( GreetSender::new );
	}

	public GreetSender( ActorContext< GreetMessage > context ){
		super( context );
	}

	@Override
	public Receive< GreetMessage > createReceive() {
		return newReceiveBuilder().onMessage( GreetMessage.class, this::onGreet ).build();
	}

	private Behavior< GreetMessage > onGreet( GreetMessage command ){
		System.out.println( "HelloWorld.GreetSender: Hello " + command.name + "!" );
		command.actorReference.tell( new GreetMessage( command.name, getContext().getSelf() ) );
		return this;
	}

}
