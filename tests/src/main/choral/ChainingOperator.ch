class ChainingExample@( A, B ) {

	Channel@( A, B ) c;

	ChainingExample@( A, B ) ChainingExample( Channel@( A, B ) c ){
		this.c = c;
	}

	static void@( A, B ) main(){
		String@B msg;
		msg = "Hello" >> this::encrypt >> c::com >> this::decrypt;
	}

	static String@A encrypt( String@A s ){ /* implementation */ }

	static String@B decrypt( String@B s ){ /* implementation */ }

}
