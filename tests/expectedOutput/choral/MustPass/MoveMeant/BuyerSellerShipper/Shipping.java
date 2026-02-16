package choral.MustPass.MoveMeant.BuyerSellerShipper;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Shipping" )
public class Shipping {
	public Shipping( String title ) {
		
	}

	public Shipping to( String address ) {
		return this;
	}
	
	public void ship() {
		
	}

}
