package DistAuth.Messages;

public class SaltMessage implements Message {

	private final String salt;

	public SaltMessage( String salt ) {
		this.salt = salt;
	}

	public String salt() {
		return salt;
	}
}

