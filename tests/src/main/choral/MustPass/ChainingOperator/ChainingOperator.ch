import choral.channels.SymChannel;

class ChainingExample@( A, B ) {

	SymChannel@( A, B ) c;

	ChainingExample@( A, B ) ChainingExample( SymChannel@( A, B ) c ){
		this.c = c;
	}

	static void main(){
		String@B msg;
		msg = "Hello"@B >> this::encrypt >> c::com >> this::decrypt;
	}

	static String@A encrypt( String@A s ){ /* implementation */ }

	static String@B decrypt( String@B s ){ /* implementation */ }

}
