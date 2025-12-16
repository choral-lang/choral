package choral.MustPass.BookSellingSoloist;

import choral.annotations.Choreography;

@Choreography( role = "Buyer", name = "Customer" )
class Customer {
	public final String name;
	public final String address;

	public Customer( String name, String address ) {
		this.name = name;
		this.address = address;
	}

}
