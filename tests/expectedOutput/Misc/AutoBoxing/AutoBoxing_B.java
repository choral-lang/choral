package Misc.AutoBoxing;

import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "B", name = "AutoBoxing" )
public class AutoBoxing_B {
	public void f() {
		f1Primitive( Unit.id );
		f1Object( Unit.id );
		f2( Unit.id );
		f3( 2 );
		f4( Unit.id );
		f5Objects( Unit.id, Unit.id );
		f5Ints( Unit.id, Unit.id );
	}
	
	public Unit f1Object( Unit i ) {
		return f1Object();
	}
	
	public Unit f1Primitive( Unit i ) {
		return f1Primitive();
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
	
	public void f5Objects( Unit o1, Unit o2 ) {
		f5Objects();
	}
	
	public void f5Ints( Unit i1, Unit i2 ) {
		f5Ints();
	}
	
	public Unit f1Object() {
		return Unit.id;
	}
	
	public Unit f1Primitive() {
		return Unit.id;
	}
	
	public void f2() {
		return;
	}
	
	public void f3() {
		
	}
	
	public void f4() {
		
	}
	
	public void f5Objects() {
		
	}
	
	public void f5Ints() {
		
	}

}
