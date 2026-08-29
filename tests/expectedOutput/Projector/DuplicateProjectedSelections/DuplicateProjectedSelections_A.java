package Projector.DuplicateProjectedSelections;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;

@Choreography( role = "A", name = "DuplicateProjectedSelections" )
class DuplicateProjectedSelections_A {
	SymChannel_A < Object > channel;

	DuplicateProjectedSelections_A( SymChannel_A < Object > channel ) {
		this.channel = channel;
	}

	void choose( String value ) {
		channel.< Choice >select( Choice.STRING );
	}
	
	void choose( Integer value ) {
		channel.< Choice >select( Choice.INTEGER );
	}
	
	void choose( boolean value ) {
		channel.< Choice >select( Choice.BOOLEAN );
	}
	
	void testSelections() {
		choose( "string" );
		choose( Integer.valueOf( 2 ) );
		choose( true );
	}

}
