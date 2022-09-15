package choral.examples.BuyerSellerShipper;

import choral.annotations.Choreography;
import choral.lang.Unit;
import choral.channels.SymChannel_A;
import choral.runtime.UI.Panel;

@Choreography( role = "Shipper", name = "BuyerSellerShipper" )
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
								String operation = cb.< String >com( Unit.id );
								Panel.show( "Shipper", "Buyer shipped " + operation );
							}
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
							}
							case False -> {

							}
						}
					}
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case False -> {

				}
			}
		}
	}

}
