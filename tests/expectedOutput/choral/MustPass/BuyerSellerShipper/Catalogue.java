package choral.MustPass.BuyerSellerShipper;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Catalogue" )
public class Catalogue {
	public void addTitle( String t, Price p ) {
		
	}
	
	public Boolean includes( String t ) {
		return true;
	}
	
	public Price quote( String t ) {
		return null;
	}
	
	public Shipping ship( String title ) {
		return null;
	}

}
