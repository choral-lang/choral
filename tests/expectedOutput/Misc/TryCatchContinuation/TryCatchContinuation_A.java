package Misc.TryCatchContinuation;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "TryCatchContinuation" )
class TryCatchContinuation_A {
	public static void signal() {
		try {
			String local = "only A";
			System.out.println( local );
		}
		catch ( Exception e ) { 
			
		}
	}

}
