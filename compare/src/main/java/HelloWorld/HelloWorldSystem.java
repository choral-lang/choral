package HelloWorld;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class HelloWorldSystem extends AbstractBehavior< HelloName > {

	private static final ActorSystem< HelloName > system = ActorSystem.create( HelloWorldSystem.create(), "hello" );

	public static void main( String[] args ) {
		system.tell( new HelloName( "World" ) );
		system.tell( new HelloName( "Akka" ) );
		system.tell( new HelloName( "Save" ) );
	}

	public static Behavior< HelloName > create(){
		return Behaviors.setup( HelloWorldSystem::new );
	}

	private final ActorRef< GreetMessage > greetSender;

	private HelloWorldSystem( ActorContext< HelloName > context ){
		super( context );
		greetSender = context.spawn( GreetSender.create(), "HelloWorld.GreetSender" );
	}

	@Override
	public Receive< HelloName > createReceive() {
		return newReceiveBuilder().onMessage( HelloName.class, this::onStart ).build();
	}

	private Behavior< HelloName > onStart( HelloName helloName ) {
		ActorRef< GreetMessage > actorReference =
				getContext().spawn( GreetReplier.create( 3 ), helloName.name );
		greetSender.tell( new GreetMessage( helloName.name, actorReference ) );
		return this;
	}
}
