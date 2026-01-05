package choral.MustPass.BookSellingSoloist;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;

@Choreography( role = "Buyer", name = "BuyBook2" )
class BuyBook2_Buyer {
	SymChannel_A < Object > c;

	BuyBook2_Buyer( SymChannel_A < Object > c ) {
		this.c = c;
	}

	void run( Customer customer ) {
		String book;
		book = Panel.prompt( "Buyer", "Insert the title of the book" );
		try {
			c.< String >com( book );
		}
		catch ( Exception e ) { 
			Panel.show( "Buyer", e );
		}
	}

}
