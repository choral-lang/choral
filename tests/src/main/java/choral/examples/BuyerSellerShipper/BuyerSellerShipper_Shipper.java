package choral.examples.BuyerSellerShipper;

import choral.runtime.UI.Panel;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

class BuyerSellerShipper_Shipper {
	SymChannel_A < Object > cb;

	BuyerSellerShipper_Shipper( Unit c, SymChannel_A < Object > cb ) {
		this( cb );
	}
	
	BuyerSellerShipper_Shipper( SymChannel_A < Object > cb ) {
		this.cb = cb;
	}

	void run( Unit catalogue, Unit customer ) {
		run();
	}
	
	void run() {
		{
			switch( cb.< EnumBoolean >select( Unit.id ) ){
				case True -> {
					{
						switch( cb.< EnumBoolean >select( Unit.id ) ){
							case True -> {
								String operation = cb.< String >com( Unit.id( Unit.id, Unit.id ) );
								Panel.show( "Shipper", "Buyer shipped " + operation );
							}
							case False -> {
								
							}
						}
					}
				}
				case False -> {
					
				}
			}
		}
	}

}
