package Misc.AutoBoxing;

import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "A", name = "AutoBoxing" )
public class AutoBoxing_A {
	public void f() {
		int numberPrimitive;
		Integer numberObject;
		numberPrimitive = f1Primitive( 2 );
		numberObject = f1Object( Integer.valueOf( 2 ) );
		numberPrimitive = Integer.valueOf( 2 );
		numberObject = 2;
		f2( Integer.valueOf( 2 ) );
		f3( Unit.id );
		f4( Integer.valueOf( 2 ) );
		f5Objects( Integer.valueOf( 2 ), Integer.valueOf( 2 ) );
		f5Ints( 2, 2 );
	}
	
	public Integer f1Object( Integer i ) {
		return i;
	}
	
	public int f1Primitive( int i ) {
		return i;
	}
	
	public void f2( int i ) {
		return;
	}
	
	public void f3( Integer i ) {
		
	}
	
	public void f3( Unit i ) {
		f3();
	}
	
	public void f4( int i ) {
		
	}
	
	public void f4( Unit i ) {
		f4();
	}
	
	public void f5Objects( Object o1, Object o2 ) {
		
	}
	
	public void f5Ints( int i1, int i2 ) {
		
	}
	
	public void f3() {
		
	}
	
	public void f4() {
		
	}

}
