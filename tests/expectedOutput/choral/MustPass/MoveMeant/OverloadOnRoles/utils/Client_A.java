package choral.MustPass.MoveMeant.OverloadOnRoles.utils;

import choral.annotations.Choreography;
import choral.lang.Unit;
import java.lang.Integer;
import java.lang.Object;

@Choreography( role = "A", name = "Client" )
public class Client_A {
	Object obj;

	public void fun( Integer in ) {
		
	}
	
	public void fun( Unit in ) {
		fun();
	}
	
	public void fun2( Integer in1, Integer in2 ) {
		
	}
	
	public void fun2( Integer in1, Unit in2 ) {
		fun2( in1 );
	}
	
	public void fun() {
		
	}
	
	public void fun2( Integer in1 ) {
		
	}

}
