package choral.MustPass.MoveMeant.utils;

import choral.annotations.Choreography;
import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
@Choreography( role = "A", name = "Price" )
public class Price {
	public final Integer amount;
	public final String currency;

	public Price( Integer amount, String currency ) {
		this.amount = amount;
		this.currency = currency;
	}

}
