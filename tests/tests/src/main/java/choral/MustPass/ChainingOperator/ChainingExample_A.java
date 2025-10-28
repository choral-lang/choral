package choral.MustPass.ChainingOperator;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "A", name = "ChainingExample" )
class ChainingExample_A {
	SymChannel_A < String > c;

	ChainingExample_A( SymChannel_A < String > c ) {
		this.c = c;
	}

	void main() {
		String msg;
		this.decrypt( c.< String >com( this.encrypt( msg = "Hello" ) ) );
	}
	
	static String encrypt( String s ) {
		return "";
	}
	
	static Unit decrypt( Unit s ) {
		return decrypt();
	}
	
	static Unit decrypt() {
		return Unit.id;
	}

}
