package choral.examples.BuyerSellerShipper;

import choral.channels.SymChannel_B;
import choral.examples.BuyerSellerShipperUtils.Customer;
import choral.runtime.UI.Panel;
import choral.examples.BuyerSellerShipperUtils.Price;
import choral.lang.Unit;

class BuyerSellerShipper_Buyer {
	SymChannel_B < Object > c;
	SymChannel_B < Object > cb;

	BuyerSellerShipper_Buyer( SymChannel_B < Object > c, SymChannel_B < Object > cb ) {
		this.c = c;
		this.cb = cb;
	}

	void run( Unit catalogue, Customer customer ) {
		run( customer );
	}
	
	void run( Customer customer ) {
		String book = Panel.prompt( "Buyer", "Insert the title of the book" );
		c.< String >com( book );
		{
			switch( c.< EnumBoolean >select( Unit.id ) ){
				case True -> {
					cb.< EnumBoolean >select( EnumBoolean.True );
					Price price = c.< Price >com( Unit.id.id( Unit.id ) );
					String choice = Panel.prompt( "Buyer", book + " costs " + price.amount + price.currency + ". Enter 'Y' to order" );
					if( choice.equals( "Y" ) ){
						c.< EnumBoolean >select( EnumBoolean.True );
						cb.< EnumBoolean >select( EnumBoolean.True );
						cb.< String >com( price.amount + price.currency );
						c.< String >com( customer.address );
					} else { 
						c.< EnumBoolean >select( EnumBoolean.False );
						cb.< EnumBoolean >select( EnumBoolean.False );
					}
				}
				case False -> {
					cb.< EnumBoolean >select( EnumBoolean.False );
				}
			}
		}
	}

}
