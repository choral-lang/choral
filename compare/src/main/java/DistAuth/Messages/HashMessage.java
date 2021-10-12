package DistAuth.Messages;

public class HashMessage implements Message {

	private final String hash;

	public HashMessage( String hash ) {
		this.hash = hash;
	}

	public String hash() {
		return hash;
	}
}
