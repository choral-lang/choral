package choral.examples.BuyerSellerShipper;

import choral.BookSelling.com.books.Customer;
import choral.BookSelling.com.books.Price;
import choral.BookSelling.com.books.Catalogue;
import org.choral.lang.Channels.SymChannel2;
import org.choral.lang.Channels.SymChannel1;
import org.choral.runtime.Enum.EnumBoolean;
import org.choral.runtime.UI.Panel;

class BuyerSellerShipper@( Seller, Buyer, Shipper ) {

	SymChannel@( Seller, Buyer )< Object > c;
	SymChannel@( Shipper, Buyer )< Object > cb;

	BuyerSellerShipper( SymChannel@( Seller, Buyer )< Object > c, SymChannel@( Shipper, Buyer )< Object > cb ) {
		this.c = c;
		this.cb = cb;
	}

	void run ( Catalogue@Seller catalogue, Customer@Buyer customer ){
		String@Buyer book = Panel@Buyer.prompt( "Buyer"@Buyer, "Insert the title of the book"@Buyer );
		String@Seller title = c.< String >com( book );
		if( catalogue.includes( title ) ){
			select( EnumBoolean@Seller.True, c );
			select( EnumBoolean@Buyer.True, cb );
			Price@Buyer price = c.< Price >com( catalogue.quote( title ) );
			String@Buyer choice = Panel@Buyer.prompt( "Buyer"@Buyer,
				book + " costs "@Buyer + price.amount + price.currency + ". Enter 'Y' to order"@Buyer
			);
			if( choice.equals( "Y"@Buyer ) ){
					select( EnumBoolean@Buyer.True, c );
					select( EnumBoolean@Buyer.True, cb );
					String@Shipper operation = cb.< String >com( price.amount + price.currency );
					Panel@Shipper.show( "Shipper"@Shipper, "Buyer shipped "@Shipper + operation );
					catalogue.ship( title ).to( c.< String >com( customer.address ) );
				} else {
					select( EnumBoolean@Buyer.False, c );
					select( EnumBoolean@Buyer.False, cb );
				}
		} else {
			select( EnumBoolean@Seller.False, c );
			select( EnumBoolean@Buyer.False, cb );
		}
	}
}
