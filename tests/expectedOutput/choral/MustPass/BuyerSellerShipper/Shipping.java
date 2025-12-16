package choral.MustPass.BuyerSellerShipper;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Shipping")
public class Shipping {
    private Shipping( String title ){
        String temp = title;
	}

    public static Shipping to( String address ){
        return new Shipping(address);
    }

    public void ship(){
    }
}
