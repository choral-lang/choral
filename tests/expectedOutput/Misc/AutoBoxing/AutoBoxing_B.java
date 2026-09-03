package Misc.AutoBoxing;

import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "B", name = "AutoBoxing" )
public class AutoBoxing_B {
	public void f() {
		f1( Unit.id );
		f1( Unit.id );
		f2( Unit.id );
		f3( 2 );
		f4( Unit.id );
		f5( Unit.id, Unit.id );
		f5( Unit.id, Unit.id );
	}
	
	public Unit f1( Unit i ) {
		return f1();
	}
	
	public void f2( Unit i ) {
		f2();
	}
	
	public void f3( Unit i ) {
		f3();
	}
	
	public void f3( Integer i ) {
		
	}
	
	public void f4( Unit i ) {
		f4();
	}
	
	public void f4( int i ) {
		
	}
	
	public void f5( Unit o1, Unit o2 ) {
		f5();
	}
	
	public Unit f1() {
		return Unit.id;
	}
	
	public void f2() {
		return;
	}
	
	public void f3() {
		
	}
	
	public void f4() {
		
	}
	
	public void f5() {
		
	}

}
