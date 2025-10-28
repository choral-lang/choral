package choral.examples.MultiFoo;

class Foo@( A, B ){}

interface Bar@( C, D )< T@( X, Y ) > {}

class SingleFoo@( E, F ){
	public static Foo@( E, F ) staticMethod(){
		return new Foo@( E, F )();
	}

	public SingleFoo( Foo@( E, F ) x ){}

}

public class MultiFoo@( E, F ) extends SingleFoo@( E, F ){

	Bar@( E, F )< Foo > x;

	public MultiFoo( SingleFoo@( E, F ) y ){
		super( super.staticMethod() ); // expectedError: Cannot reference 'super' before supertype constructor has been called
	}

}
