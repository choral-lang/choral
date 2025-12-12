package choral.MustPass.BuyerSellerShipper;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Price")
public class Price {
    public final Integer amount;
	public final String currency;

	public Price( Integer amount, String currency ){
        this.amount = amount;
        this.currency = currency;
	}
}
