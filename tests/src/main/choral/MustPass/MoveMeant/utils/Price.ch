package choral.MustPass.MoveMeant.utils;

import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
public class Price@A {

	public final Integer@A amount;
	public final String@A currency;

	public Price( Integer@A amount, String@A currency ){
		this.amount = amount;
		this.currency = currency;
	}

}
