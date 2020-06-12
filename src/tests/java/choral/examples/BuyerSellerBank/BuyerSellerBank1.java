package choral.examples.BuyerSellerBank;

import choral.BookSelling.com.books.Catalogue;
import choral.BookSelling.com.books.Price;
import org.choral.lang.Channels.SymChannel1;
import org.choral.runtime.Enum.EnumBoolean;
import org.choral.lang.Unit;

class BuyerSellerBank1 {
	SymChannel1< Object > c;

	BuyerSellerBank1( SymChannel1 < Object > c, Unit cb ) {
		this.c = c;
	}

	BuyerSellerBank1( SymChannel1 < Object > c ) {
		this( c, Unit.id );
	}

	void run( Catalogue catalogue, Unit customer ) {
		run( catalogue );
	}

	void run( Catalogue catalogue ) {
		String title;
		title = c.< String >com( Unit.id );
		if( catalogue.includes( title ) ){
			c.< EnumBoolean >select( EnumBoolean.True );
			c.< Price >com( catalogue.quote( title ) );
			{
				switch( c.< EnumBoolean >select( Unit.id ) ){
					default -> {
						throw new RuntimeException( "Received unexpected label from select operation" );
					}
					case True -> {
						catalogue.ship( title ).to( c.< String >com( Unit.id ) );
					}
					case False -> {

					}
				}
			}
		} else {
			c.< EnumBoolean >select( EnumBoolean.False );
		}
	}
}
