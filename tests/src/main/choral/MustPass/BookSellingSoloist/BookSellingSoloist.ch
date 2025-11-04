package choral.MustPass.BookSellingSoloist;

import choral.channels.SymChannel;

enum EnumBoolean@A{ True, False }

class Panel@A {

	private Panel(){}

	public static String@A prompt( String@A world, String@A prompt ) {
		return ""@A;
	}

	public static void show( String@A world, Object@A text ) {
	}

}

class Catalogue@A {

	public void addTitle( String@A t, int@A p ){
	}

	public Boolean@A includes( String@A t ){
		if (t == ""@A){
			return false@A;
		}
		else {
			return true@A;
		}
	}

	public Integer@A quote( String@A t ){
		return 0@A;
	}

}

class BuyBook1@(Seller, Buyer) {
	SymChannel@(Seller, Buyer)< Object > c;

	BuyBook1( SymChannel@(Seller, Buyer)< Object > c ) {
		this.c = c;
	}

	void run ( Catalogue@Buyer catalogue ){
		String@Buyer title;
		title = null@Buyer;
		try { 
			title = c.< String >com( ""@Seller );
		} catch ( Exception@Seller e ){
			Panel@Seller.show( "Seller"@Seller, e );
		}
		if( catalogue.includes( title ) ){
			c.<EnumBoolean >select( EnumBoolean@Seller.True );
			c.< Integer >com( catalogue.quote( title ) );
		} else {
			c.< EnumBoolean >select( EnumBoolean@Seller.False );
		}
	}
}

class Customer@Buyer {
	public final String@Buyer name;
	public final String@Buyer address;

	public Customer( String@Buyer name, String@Buyer address ){
	}

}

class BuyBook2@(Buyer, Seller){

	SymChannel@(Buyer, Seller)< Object > c;

	BuyBook2( SymChannel@(Buyer, Seller)< Object > c ) {
		this.c = c;
	}

	void run ( Customer@Buyer customer ){
		String@Buyer book;
		book = Panel@Buyer.prompt( "Buyer"@Buyer, "Insert the title of the book"@Buyer );
		try {
			c.< String >com( book );
		} catch ( Exception@Buyer e ){
			Panel@Buyer.show( "Buyer"@Buyer, e );
		}
	}
}