package choral.MustPass.BuyerSellerShipper;

import choral.MustPass.BuyerSellerShipperUtils.Customer;
import choral.MustPass.BuyerSellerShipperUtils.Price;
import choral.MustPass.BuyerSellerShipperUtils.Catalogue;
import choral.channels.SymChannel;
import choral.runtime.UI.Panel;

enum EnumBoolean@A{ True, False }

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
			c.< EnumBoolean >select( EnumBoolean@Seller.True );
			cb.< EnumBoolean >select( EnumBoolean@Buyer.True );
			Price@Buyer price = c.< Price >com( catalogue.quote( title ) );
			String@Buyer choice = Panel@Buyer.prompt( "Buyer"@Buyer,
				book + " costs "@Buyer + price.amount + price.currency + ". Enter 'Y' to order"@Buyer
			);
			if( choice.equals( "Y"@Buyer ) ){
					c.< EnumBoolean >select( EnumBoolean@Buyer.True );
					cb.< EnumBoolean >select( EnumBoolean@Buyer.True );
					String@Shipper operation = cb.< String >com( price.amount + price.currency );
					Panel@Shipper.show( "Shipper"@Shipper, "Buyer shipped "@Shipper + operation );
					catalogue.ship( title ).to( c.< String >com( customer.address ) );
				} else {
					c.< EnumBoolean >select( EnumBoolean@Buyer.False );
					cb.< EnumBoolean >select( EnumBoolean@Buyer.False );
				}
		} else {
			c.< EnumBoolean >select( EnumBoolean@Seller.False );
			cb.< EnumBoolean >select( EnumBoolean@Buyer.False );
		}
	}
}
