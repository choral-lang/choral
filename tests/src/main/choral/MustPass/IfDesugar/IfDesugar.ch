import java.utils.Random;

class IfDesugarTest@( P ) {

	void@( P ) main() {
		if( Random@( P ).nextBoolean() ){
			System@( P ).out.println( "Even" );
		} else {
			System@( P ).out.println( "Odd" );
		}
		match( Random@( P ).nextBoolean() ){
			True : {
				System@( P ).out.println( "Even" );
			}
			False f : {
				System@( P ).out.println( "Odd" );
			}
		}
	}

}
