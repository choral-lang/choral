package choral.amend.BuyerSellerShipper;

import choral.amend.BuyerSellerShipperUtils.Customer;
import choral.amend.BuyerSellerShipperUtils.Price;
import choral.amend.BuyerSellerShipperUtils.Catalogue;
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
		String@Seller title = book;
		if( catalogue.includes( title ) ){
			

			Price@Buyer price = catalogue.quote( title );
			String@Buyer choice = Panel@Buyer.prompt( "Buyer"@Buyer,
				book + " costs "@Buyer + price.amount + price.currency + ". Enter 'Y' to order"@Buyer
			);
			if( choice.equals( "Y"@Buyer ) ){
				

					String@Shipper operation = price.amount + price.currency;
					Panel@Shipper.show( "Shipper"@Shipper, "Buyer shipped "@Shipper + operation );
					catalogue.ship( title ).to( customer.address );
				} else {
					

				}
		} else {
			

		}
	}
}
