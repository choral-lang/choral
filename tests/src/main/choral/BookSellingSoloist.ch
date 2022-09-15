//import choral.BookSelling.com.books.*;
//import com.choral.runtime.UI.Panel;
//import com.choral.runtime.Channel1;
//import com.choral.runtime.Channel2;
//import com.choral.runtime.Enum.EnumBoolean;

class BuyBook1@Seller {
Channel1@Seller< Object > c;

BuyBook1( Channel1@Seller< Object > c ) {
		this.c = c;
}

void@Seller run ( Catalogue@Seller catalogue ){
	String@Seller title;
	title = null@Seller;
	try {
		title = c.< String >com();
	} catch ( Exception@Seller e ){
		Panel@Seller.show( "Seller"@Seller, e.getStackTrace().toString() );
	}
	if( catalogue.includes( title ) ){
		select( EnumBoolean@Seller.True, c );
		c.< Price >com( catalogue.quote( title ) );
		match( c.< EnumBoolean >com() ){
			True : {
				catalogue.ship( title ).to( c.< String >com() );
			}
			False : {}
		}
	} else {
		select( EnumBoolean@Seller.False, c );
	}
}

}

class BuyBook2@Buyer{

	Channel2@Buyer< Object > c;

	BuyBook2( Channel2@Buyer< Object > c ) {
			this.c = c;
	}

	void@Buyer run ( Customer@Buyer customer ){
		String@Buyer book;
		book = Panel@Buyer.prompt( "Buyer"@Buyer, "Insert the title of the book"@Buyer );
		try {
				c.< String >com( book );
		} catch ( Exception@Buyer e ){
			Panel@Buyer.show( "Buyer"@Buyer, e.getStackTrace().toString() );
		}
		match( c.< EnumBoolean >com() ){
			True : {
				Price@Buyer price;
				price = c.< Price >com();
				String@Buyer choice;
				choice = Panel@Buyer.prompt( "Buyer"@Buyer,
					book + " costs "@Buyer + price.amount + price.currency + ". Enter 'Y' to order"@Buyer
				);
				if( EnumBoolean@Buyer.fromBoolean( choice.equals( "Y"@Buyer ) ) ){
					select( EnumBoolean@Buyer.True, c );
					c.< String >com( customer.address );
				} else {
					select( EnumBoolean@Buyer.False, c );
				}
			}
			False: {}
		}
	}
}
