package Projector.DuplicateProjectedCommunication;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "DuplicateProjectedCommunication" )
class DuplicateProjectedCommunication_B {
	SymChannel_B < Object > channel;

	DuplicateProjectedCommunication_B( SymChannel_B < Object > channel ) {
		this.channel = channel;
	}

	Unit identity( Unit value ) {
		return identity();
	}
	
	void accept( Unit left, Unit right ) {
		accept();
	}
	
	Object communicate( Unit value ) {
		return communicate();
	}
	
	void testCommunication() {
		communicate( Unit.id );
		communicate( Unit.id );
	}
	
	Unit identity() {
		return Unit.id;
	}
	
	void accept() {
		
	}
	
	Object communicate() {
		return channel.< Object >com( Unit.id );
	}

}
