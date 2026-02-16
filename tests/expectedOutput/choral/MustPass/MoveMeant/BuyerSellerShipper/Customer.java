package choral.MustPass.MoveMeant.BuyerSellerShipper;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Customer" )
public class Customer {
	public final String name;
	public final String address;

	public Customer( String name, String address ) {
		this.name = name;
		this.address = address;
	}

}
