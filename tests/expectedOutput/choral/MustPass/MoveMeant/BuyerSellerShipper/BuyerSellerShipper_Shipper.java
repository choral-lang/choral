package choral.MustPass.MoveMeant.BuyerSellerShipper;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;
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
		switch( cb.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				switch( cb.< KOCEnum >select( Unit.id ) ){
					case CASE0 -> {
						Integer msg2 = cb.< Integer >com( Unit.id );
						String msg3 = cb.< String >com( Unit.id );
						String operation = msg2 + msg3;
						Panel.show( "Shipper", "Buyer shipped " + operation );
					}
					case CASE1 -> {
						
					}
					default -> {
						throw new RuntimeException( "Received unexpected label from select operation" );
					}
				}
			}
			case CASE1 -> {
				
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
