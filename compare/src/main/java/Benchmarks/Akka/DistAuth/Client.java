package Benchmarks.Akka.DistAuth;

import Benchmarks.Akka.DistAuth.DistAuthUtils.Base64_Encoder;
import Benchmarks.Akka.DistAuth.DistAuthUtils.Credentials;
import Benchmarks.Akka.DistAuth.Messages.*;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Client extends AbstractBehavior< Message > {

	private final ActorRef< Message > ip;
	private final Credentials credentials;
	private final ActorRef< SystemDemo.AuthSession > system;

	public Client(
			ActorRef< SystemDemo.AuthSession > system,
			ActorRef< Message > ip,
			ActorRef< Message > service,
			Credentials credentials,
			ActorContext< Message > context
	) {
		super( context );
		this.system = system;
		this.ip = ip;
		this.credentials = credentials;
		ip.tell( new AuthRequest( getContext().getSelf(), service, credentials.username ) );
	}

	public static Behavior< Message > create(
			ActorRef< SystemDemo.AuthSession > system,
			ActorRef< Message > ip, ActorRef< Message > service, Credentials credentials
	) {
		return Behaviors.setup( c -> new Client( system, ip, service, credentials, c ) );
	}

	@Override
	public Receive< Message > createReceive() {
		return newReceiveBuilder()
				.onMessage( SaltMessage.class, this::onMessage )
				.onMessage( AuthResultMessage_A.class, this::onMessage )
				.build();
	}

	private Behavior< Message > onMessage( SaltMessage message ) {
		String hash = calcHash( message.salt(), credentials.password );
		ip.tell( new HashMessage( hash ) );
		return this;
	}

	private Behavior< Message > onMessage( AuthResultMessage_A message ) {
		if( message.result().left().isPresent() ) {
			System.out.println( "[Client] token id: " + message.result().left().get().id() );
		} else {
			System.out.println( "[Client] Something went wrong!" );
		}
		system.tell( new SystemDemo.AuthSessionStop() );
		return this;
	}

	private String calcHash( String salt, String pwd ) {
		String salt_and_pwd;
		salt_and_pwd = salt + pwd;
		try {
			MessageDigest md;
			md = MessageDigest.getInstance( "SHA3-256" );
			return Base64_Encoder.encodeToString(
					md.digest( salt_and_pwd.getBytes( StandardCharsets.UTF_8 ) ) );
		} catch( NoSuchAlgorithmException e ) {
			e.printStackTrace();
			return "Algorithm not found";
		}
	}

}
