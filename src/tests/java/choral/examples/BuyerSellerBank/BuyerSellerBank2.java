package choral.examples.BuyerSellerBank;

import choral.BookSelling.com.books.Customer;
import choral.BookSelling.com.books.Price;
import org.choral.lang.Channels.SymChannel2;
import org.choral.runtime.Enum.EnumBoolean;
import org.choral.runtime.UI.Panel;
import org.choral.lang.Unit;

class BuyerSellerBank2 {
	SymChannel2< Object > c;
	SymChannel2 < Object > cb;

	BuyerSellerBank2( SymChannel2 < Object > c, SymChannel2 < Object > cb ) {
		this.c = c;
		this.cb = cb;
	}

	void run( Unit catalogue, Customer customer ) {
		run( customer );
	}

	void run( Customer customer ) {
		String book;
		book = Panel.prompt( "Buyer", "Insert the title of the book" );
		c.< String >com( book );
		{
			switch( c.< EnumBoolean >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case True -> {
					cb.< EnumBoolean >select( EnumBoolean.True );
					Price price;
					price = c.< Price >com( Unit.id );
					String choice;
					choice = Panel.prompt( "Buyer", book + " costs " + price.amount + price.currency + ". Enter 'Y' to order" );
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
