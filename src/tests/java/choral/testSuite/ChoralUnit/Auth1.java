package choral.testSuite.ChoralUnit;

import org.choral.lang.Channels.SymChannel1;
import org.choral.runtime.Enum.EnumBoolean;
import org.choral.lang.Unit;

public class Auth1 {
	SymChannel1< Object > c;

	public Auth1( SymChannel1< Object > c ) {
		this.c = c;
	}

	public EnumBoolean run( String username, String password ) {
		c.< String >com( username );
		c.< String >com( password );
		switch( 1 ){
			case 1 -> {
				System.out.println( "1" );
			}
			case 2 -> {
				System.out.println( "1" );
			}
			default -> {
				System.out.println( "NOT 1!" );
			}
		}
		{
			switch( c.< EnumBoolean >com( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case True -> {
					return c.< EnumBoolean >com( Unit.id );
				}
				case False -> {
					return c.< EnumBoolean >com( Unit.id );
				}
			}
		}
	}

	public Unit checkID( Unit i ) {
		return checkID();
	}

	public Unit checkID() {
		return c.< Integer >com( c.< Integer >com( Unit.id ) );
	}

}
