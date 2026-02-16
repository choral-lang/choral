package choral.MustPass.MoveMeant.BuyerSellerShipper;

import choral.MustPass.MoveMeant.BuyerSellerShipper.Catalogue;
import choral.MustPass.MoveMeant.BuyerSellerShipper.Price;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "Seller", name = "BuyerSellerShipper" )
class BuyerSellerShipper_Seller {
	SymChannel_A < Object > c;

	BuyerSellerShipper_Seller( SymChannel_A < Object > c, Unit cb ) {
		this( c );
	}
	
	BuyerSellerShipper_Seller( SymChannel_A < Object > c ) {
		this.c = c;
	}

	void run( Catalogue catalogue, Unit customer ) {
		run( catalogue );
	}
	
	void run( Catalogue catalogue ) {
		String msg0 = c.< String >com( Unit.id );
		String title = msg0;
		if( catalogue.includes( title ) ){
			c.< KOCEnum >select( KOCEnum.CASE0 );
			c.< Price >com( catalogue.quote( title ) );
			switch( c.< KOCEnum >select( Unit.id ) ){
				case CASE0 -> {
					String msg4 = c.< String >com( Unit.id );
					catalogue.ship( title ).to( msg4 );
				}
				case CASE1 -> {
					
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		} else { 
			c.< KOCEnum >select( KOCEnum.CASE1 );
		}
	}

}
