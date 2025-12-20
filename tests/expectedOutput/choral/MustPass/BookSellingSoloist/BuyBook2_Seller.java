package choral.MustPass.BookSellingSoloist;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "Seller", name = "BuyBook2" )
class BuyBook2_Seller {
	SymChannel_B < Object > c;

	BuyBook2_Seller( SymChannel_B < Object > c ) {
		this.c = c;
	}

	void run( Unit customer ) {
		run();
	}
	
	void run() {
		{
			c.< String >com( Unit.id );
		}
	}

}
