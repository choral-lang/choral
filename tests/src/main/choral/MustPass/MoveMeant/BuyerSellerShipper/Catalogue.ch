package choral.MustPass.MoveMeant.BuyerSellerShipper;

import choral.MustPass.MoveMeant.shared.utils.Price;

public class Catalogue@A {

	public void addTitle( String@A t, Price@A p ){
	}

	public Boolean@A includes( String@A t ){
		return true@A;
	}

	public Price@A quote( String@A t ){
		return new Price@A(1@A, t);
	}

	public Shipping@A ship( String@A title ){
		return new Shipping@A(title);
	}

}
