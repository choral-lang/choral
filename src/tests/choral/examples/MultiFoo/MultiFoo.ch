package choral.examples.MultiFoo;

interface Foo@( A, B ){}

interface Bar@( C, D )< T@( X, Y ) > {}

public class MultiFoo@( E, F ){

	Bar@( E, F )< Foo > x;

}
