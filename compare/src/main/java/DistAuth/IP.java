package DistAuth;

import DistAuth.DistAuthUtils.AuthResult_A;
import DistAuth.DistAuthUtils.AuthResult_B;
import DistAuth.DistAuthUtils.AuthToken;
import DistAuth.DistAuthUtils.ClientRegistry;
import DistAuth.Messages.*;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class IP extends AbstractBehavior< Message > {

	private ActorRef< Message > client;
	private ActorRef< Message > service;
	private final ActorRef< SystemDemo.AuthSession > system;

	public IP(
			ActorContext< Message > context,
			ActorRef< SystemDemo.AuthSession > system
	) {
		super( context );
		this.system = system;
	}

	public static Behavior< Message > create(
			ActorRef< SystemDemo.AuthSession > system
	) {
		return Behaviors.setup( context -> new IP( context, system ) );
	}

	@Override
	public Receive< Message > createReceive() {
		return newReceiveBuilder()
				.onMessage( AuthRequest.class, this::onMessage )
				.onMessage( HashMessage.class, this::onMessage )
				.build();
	}

	private Behavior< Message > onMessage( AuthRequest request ) {
		this.client = request.client();
		this.service = request.service();
		String salt = ClientRegistry.getSalt( request.username() );
		client.tell( new SaltMessage( salt ) );
		return this;
	}

	private Behavior< Message > onMessage( HashMessage message ) {
		if( ClientRegistry.check( message.hash() ) ) {
			AuthToken token = AuthToken.create();
			client.tell( new AuthResultMessage_A( new AuthResult_A( token ) ) );
			service.tell( new AuthResultMessage_B( new AuthResult_B( token ) ) );
		} else {
			client.tell( new AuthResultMessage_A( new AuthResult_A() ) );
			service.tell( new AuthResultMessage_B( new AuthResult_B() ) );
		}
		system.tell( new SystemDemo.AuthSessionStop() );
		return this;
	}
}
