import choral.channels.SymChannel_B;
import choral.lang.Unit;
import choral.annotations.Choreography;

@Choreography( role = "B", name = "ChainingExample" )
class ChainingExample_B {
	SymChannel_B < String > c;

	ChainingExample_B( SymChannel_B < String > c ) {
		this.c = c;
	}

	void main() {
		this.decrypt( c.< String >com( this.encrypt( Unit.id ) ) );
	}
	
	static Unit encrypt( Unit s ) {
		return encrypt();
	}
	
	static String decrypt( String s ) {
		return "";
	}
	
	static Unit encrypt() {
		return Unit.id;
	}

}
