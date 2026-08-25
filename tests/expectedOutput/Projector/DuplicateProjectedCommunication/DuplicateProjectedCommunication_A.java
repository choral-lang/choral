package Projector.DuplicateProjectedCommunication;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "A", name = "DuplicateProjectedCommunication" )
class DuplicateProjectedCommunication_A {
	SymChannel_A < Object > channel;

	DuplicateProjectedCommunication_A( SymChannel_A < Object > channel ) {
		this.channel = channel;
	}

	Integer identity( Integer value ) {
		return value;
	}
	
	int identity( int value ) {
		return value;
	}
	
	void accept( Object left, Object right ) {
		
	}
	
	void accept( int left, int right ) {
		
	}
	
	Unit communicate( String value ) {
		return channel.< Object >com( "sent by the String overload" );
	}
	
	Unit communicate( Integer value ) {
		return channel.< Object >com( Integer.valueOf( 42 ) );
	}
	
	void testCommunication() {
		communicate( "hello" );
		communicate( Integer.valueOf( 42 ) );
	}

}
