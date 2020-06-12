package choral.testSuite.ChoralUnit;

import org.choral.lang.Channels.SymChannel2;
import org.choral.runtime.Enum.EnumBoolean;
import org.choral.lang.Unit;

public class Auth2 {
	SymChannel2< Object > c;

	public Auth2( SymChannel2< Object > c ) {
		this.c = c;
	}

	public Unit run( Unit username, Unit password ) {
		return run();
	}

	public Boolean checkID( Integer i ) {
		return i == c.< Integer >com( c.< Integer >com( i ) );
	}

	public Unit run() {
		String user;
		user = c.< String >com( Unit.id );
		String psw;
		psw = c.< String >com( Unit.id );
		if( user.equals( "USR" ) && psw.equals( "PSW" ) ){
			c.< EnumBoolean >com( EnumBoolean.True );
			return c.< EnumBoolean >com( EnumBoolean.True );
		} else {
			c.< EnumBoolean >com( EnumBoolean.False );
			return c.< EnumBoolean >com( EnumBoolean.False );
		}
	}

}
