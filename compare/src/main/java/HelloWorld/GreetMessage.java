package HelloWorld;

import akka.actor.typed.ActorRef;

public final class GreetMessage {
	public final String name;
	public final ActorRef< GreetMessage > actorReference;

	public GreetMessage( String sender, ActorRef< GreetMessage > actorReference ) {
		this.name = sender;
		this.actorReference = actorReference;
	}
}
