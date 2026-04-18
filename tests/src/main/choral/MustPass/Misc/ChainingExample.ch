package choral.MustPass.ChainingOperator;

import choral.channels.SymChannel;

class ChainingExample@( A, B ) {

	SymChannel@( A, B )<String> c;

	ChainingExample( SymChannel@( A, B )<String> c ){
		this.c = c;
	}

	void main(){
		String@A msg;
		msg = "Hello"@A >> this::encrypt >> c::<String>com >> this::decrypt;
	}

	static String@A encrypt( String@A s ){ return ""@A; }

	static String@B decrypt( String@B s ){ return ""@B; }

}
