package choral.examples.BuyerSellerShipperUtils;

public class Shipping {
	private String address;
	private String title;

	public Shipping( String title ) {
		this.title = title;
	}

	public Shipping to( String address ) {
		this.address = address;
		return this;
	}

	public void ship() {
		System.out.println( "Shipping '" + title + "' to '" + address + "'" );
	}

}
