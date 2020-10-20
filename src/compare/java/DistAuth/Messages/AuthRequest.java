package DistAuth.Messages;

import akka.actor.typed.ActorRef;

public class AuthRequest implements Message {
	final private ActorRef< Message > client;
	final private ActorRef< Message > service;
	final private String username;

	public AuthRequest(
			ActorRef< Message > client,
			ActorRef< Message > service,
			String username
	) {
		this.client = client;
		this.service = service;
		this.username = username;
	}

	public ActorRef< Message > client() {
		return client;
	}

	public ActorRef< Message > service() {
		return service;
	}

	public String username() {
		return username;
	}
}
