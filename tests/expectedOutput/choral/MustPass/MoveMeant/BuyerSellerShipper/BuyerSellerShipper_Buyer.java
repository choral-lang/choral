package choral.MustPass.MoveMeant.BuyerSellerShipper;

import choral.MustPass.MoveMeant.BuyerSellerShipper.Customer;
import choral.MustPass.MoveMeant.BuyerSellerShipper.Price;
import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;
import choral.runtime.UI.Panel;

@Choreography( role = "Buyer", name = "BuyerSellerShipper" )
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
		switch( c.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				cb.< KOCEnum >select( KOCEnum.CASE0 );
				Price msg1 = c.< Price >com( Unit.id );
				Price price = msg1;
				String choice = Panel.prompt( "Buyer", book + " costs " + price.amount + price.currency + ". Enter 'Y' to order" );
				if( choice.equals( "Y" ) ){
					cb.< KOCEnum >select( KOCEnum.CASE0 );
					c.< KOCEnum >select( KOCEnum.CASE0 );
					cb.< Integer >com( price.amount );
					cb.< String >com( price.currency );
					c.< String >com( customer.address );
				} else { 
					cb.< KOCEnum >select( KOCEnum.CASE1 );
					c.< KOCEnum >select( KOCEnum.CASE1 );
				}
			}
			case CASE1 -> {
				cb.< KOCEnum >select( KOCEnum.CASE1 );
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
