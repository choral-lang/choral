package Misc.TryCatchContinuation;

class TryCatchContinuation@( A, B ) {
	public static void signal() {
		try {
			String@A local = "only A"@A;
			System@A.out.println( local );
		} catch ( Exception@A e ) {
		}
		System@B.out.println( "after try"@B );
	}
}
