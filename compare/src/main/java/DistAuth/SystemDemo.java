package DistAuth;

import DistAuth.DistAuthUtils.Credentials;
import DistAuth.Messages.Message;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class SystemDemo extends AbstractBehavior< SystemDemo.AuthSession > {

	public interface AuthSession {}
	public static class AuthSessionStart implements AuthSession {}
	public static class AuthSessionStop implements AuthSession {}

	private static final ActorSystem< SystemDemo.AuthSession > system = ActorSystem.create( SystemDemo.create(), "distauth_session" );

	public static void main( String[] args ) {
		system.tell( new AuthSessionStart() );
	}

	public static Behavior< AuthSession > create(){
		return Behaviors.setup( SystemDemo::new );
	}

	private SystemDemo( ActorContext< AuthSession > context ){
		super( context );
	}

	@Override
	public Receive< AuthSession > createReceive() {
		return newReceiveBuilder()
				.onMessage( AuthSessionStart.class, this::onStart )
				.onMessage( AuthSessionStop.class, this::onStop )
				.build();
	}

	private Behavior< AuthSession > onStart( AuthSessionStart session ) {
		// we create all actors
		try {
			ActorRef< Message > ip = getContext().spawn( IP.create( getContext().getSelf() ), "IP" );
			ActorRef< Message > service = getContext().spawn( Service.create( getContext().getSelf() ), "service" );
			getContext().spawn( Client.create( getContext().getSelf(), ip, service, new Credentials( "john", "doe" ) ), "client" );
		} catch( Exception e ){
			e.printStackTrace();
		}
		spawnedActors = 3;
		return this;
	}

	private int spawnedActors;
	private Behavior< AuthSession > onStop( AuthSessionStop message ){
		spawnedActors--;
		return spawnedActors < 1 ? Behaviors.stopped() : this;
	}

}
