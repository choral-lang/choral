package choral.MustPass.MoveMeant.BuyerSellerShipper;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Catalogue" )
public class Catalogue {
	public void addTitle( String t, Price p ) {
		
	}
	
	public Boolean includes( String t ) {
		return true;
	}
	
	public Price quote( String t ) {
		return new Price( 1, t );
	}
	
	public Shipping ship( String title ) {
		return new Shipping( title );
	}

}
