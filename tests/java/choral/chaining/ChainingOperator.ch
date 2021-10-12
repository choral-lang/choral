class ChainingExample@( A, B ) {

	Channel@( A, B ) c;

	ChainingExample@( A, B ) ChainingExample( Channel@( A, B ) c ){
		this.c = c;
	}

	static void@( A, B ) main(){
		String@B msg;
		msg = "Hello"
			>> Random@A::nextBoolean
			>> String@A::new
			>> EnumBoolean@A.True::new
			>> this::encrypt
			>> c::com
			>> System@A.out::println
			>> this::decrypt;
		System@B.out.println( msg );
	}

	static String@A encrypt( String@A s ){ /* implementation */ }

	static String@B decrypt( String@B s ){ /* implementation */ }

}
