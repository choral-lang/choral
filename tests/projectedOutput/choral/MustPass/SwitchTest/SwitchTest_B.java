package choral.MustPass.SwitchTest;

import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "B", name = "SwitchTest" )
public class SwitchTest_B {
	Unit m1( Unit c ) {
		return m1();
	}
	
	Unit m2( Unit c ) {
		return m2();
	}
	
	Unit m3( Unit c ) {
		return m3();
	}
	
	Unit m1() {
		{
			return Unit.id;
		}
		return Unit.id;
	}
	
	Unit m2() {
		{
			return Unit.id;
		}
	}
	
	Unit m3() {
		{
			return Unit.id;
		}
	}

}
