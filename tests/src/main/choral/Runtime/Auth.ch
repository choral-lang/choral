package choral.testSuite.ChoralUnit;

public class Auth@( A, B ) {

	SymChannel@( A, B )< Object > c;

	public Auth( SymChannel@( A, B )< Object > c ){
		this.c = c;
	}

	public EnumBoolean@A run( String@A username, String@A password ){
		String@B user = c.< String >com( username );
		String@B psw = c.< String >com( password );
		switch( 1@A ){
			case 1@A, 2@A -> { System@A.out.println( "1"@A ); }
			default -> { System@A.out.println( "NOT 1!"@A ); }
		}
		if( user.equals( "USR"@B ) && psw.equals( "PSW"@B ) ){
			select( EnumBoolean@B.True, c );
			return c.< EnumBoolean >com( EnumBoolean@B.True );
		} else {
			select( EnumBoolean@B.False, c );
			return c.< EnumBoolean >com( EnumBoolean@B.False );
		}
	}

	public Boolean@B checkID( Integer@B i ){
		return i == c.< Integer>com( c.< Integer >com( i ) );
	}

}
