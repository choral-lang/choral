package choral.MustPass.MoveMeant.OverloadOnRoles;

import choral.annotations.Choreography;
import choral.lang.Unit;
import java.lang.Integer;

@Choreography( role = "B", name = "Client" )
class Client_B {
	public void fun( Unit in ) {
		fun();
	}
	
	public void fun( Integer in ) {
		
	}
	
	public void fun2( Unit in1, Unit in2 ) {
		fun2();
	}
	
	public void fun2( Unit in1, Integer in2 ) {
		fun2( in2 );
	}
	
	public void fun() {
		
	}
	
	public void fun2() {
		
	}
	
	public void fun2( Integer in2 ) {
		
	}

}
