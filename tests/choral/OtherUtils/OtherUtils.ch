package OtherUtils;

class OtherUtils@( A, B ) {

	static void doublePrint( String@A ss, String@B sb ){
		System@A.out.println( "A: "@A + ss );
		System@B.out.println( "B: "@B + sb );
	}
}
