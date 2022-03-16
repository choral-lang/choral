package choral.examples.RetwisChoral;

import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
public class Token {

	private final String id;

	public Token( String id ) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Token{" +
				"id='" + id + '\'' +
				'}';
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals( Object o ) {
		if( this == o ) return true;
		if( o == null || getClass() != o.getClass() ) return false;
		Token token = (Token) o;
		return id.equals( token.id );
	}

	public String id() {
		return this.id;
	}

}