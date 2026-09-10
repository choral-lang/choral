package Projector.DuplicateProjectedSelections;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "DuplicateProjectedSelections" )
class DuplicateProjectedSelections_B {
	SymChannel_B < Object > channel;
	int result;

	DuplicateProjectedSelections_B( SymChannel_B < Object > channel ) {
		this.channel = channel;
	}

	void choose( Unit value ) {
		choose();
	}
	
	void testSelections() {
		choose( Unit.id );
		choose( Unit.id );
		choose( Unit.id );
	}
	
	void choose() {
		switch( channel.< Choice >select( Unit.id ) ){
			case STRING -> {
				result = 1;
			}
			case INTEGER -> {
				result = 2;
			}
			case BOOLEAN -> {
				result = 3;
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
