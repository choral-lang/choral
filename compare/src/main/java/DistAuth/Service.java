package DistAuth;

import DistAuth.Messages.AuthResultMessage_B;
import DistAuth.Messages.Message;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Service extends AbstractBehavior< Message > {

	public Service(
			ActorContext< Message > context,
			ActorRef< SystemDemo.AuthSession > system
	) {
		super( context );
		this.system = system;
	}

	private final ActorRef< SystemDemo.AuthSession > system;
	public static Behavior< Message > create(
			ActorRef< SystemDemo.AuthSession > system
	) {
		return Behaviors.setup( context -> new Service( context, system ) );
	}

	@Override
	public Receive< Message > createReceive() {
		return newReceiveBuilder()
				.onMessage( AuthResultMessage_B.class, this::onMessage )
				.build();
	}

	private Behavior< Message > onMessage( AuthResultMessage_B message ) {
		if( message.result().right().isPresent() ){
			System.out.println( "[Service] token id: " + message.result().right().get().id() );
		} else {
			System.out.println( "[Service] Something went wrong!");
		}
		system.tell( new SystemDemo.AuthSessionStop() );
		return this;
	}

}
