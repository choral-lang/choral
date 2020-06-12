package choral.examples.BuyerSellerBank;

import org.choral.lang.Channels.SymChannel1;
import org.choral.runtime.Enum.EnumBoolean;
import org.choral.runtime.UI.Panel;
import org.choral.lang.Unit;

class BuyerSellerBank3 {
	SymChannel1< Object > cb;

	BuyerSellerBank3( Unit c, SymChannel1 < Object > cb ) {
		this.cb = cb;
	}

	BuyerSellerBank3( SymChannel1 < Object > cb ) {
		this( Unit.id, cb );
	}

	void run( Unit catalogue, Unit customer ) {
		run();
	}

	void run() {
		{
			switch( cb.< EnumBoolean >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case True -> {
					{
						switch( cb.< EnumBoolean >select( Unit.id ) ){
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
							}
							case True -> {
								String operation;
								operation = cb.< String >com( Unit.id );
								Panel.show( "Bank", "Buyer withdrew " + operation );
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
