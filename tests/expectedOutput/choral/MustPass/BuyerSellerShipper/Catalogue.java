package choral.MustPass.BuyerSellerShipper;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Catalogue")
public class Catalogue {
    public void addTitle( String t, Price p ){
	}

    public boolean includes (String t) {
        return t.isEmpty();
    }

    public Price quote( String t ){
        return new Price(0, t);
	}

    public Shipping ship( String title ){
        return Shipping.to(title);
	}
}
