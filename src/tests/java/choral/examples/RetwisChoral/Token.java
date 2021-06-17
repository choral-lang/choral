package choral.examples.RetwisChoral;

import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
public class Token{

	private final String id;

	public Token( String id ) {
		this.id = id;
	}
}