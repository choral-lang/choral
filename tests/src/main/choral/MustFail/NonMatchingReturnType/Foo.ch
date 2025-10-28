package foo;

public class Foo@A<T@A> {

}

abstract class C0@A{

	public abstract <T@X> void m (Object@A y);

    public abstract <T@X extends Integer@X> void m (T@A y);

}

class C1@A implements I0@A, I1@A, I2@A {

	@Override
	public void m (Object@A y) {
	    Integer@A z = 5@A;
		new C1@A().<Integer>m( z );
	}

}

class C2@A implements I1@A, I2@A {

    public void m (Object@A y) {
	    Integer@A z = 5@A;
		new C2@A().<Integer>m( z );
	}

}

class C4@A<T@X extends I0@X & I3@X> { // expectedError: Method 'm(java.lang.Object@(X))' in 'foo.I3@(X)' clashes with method 'm(java.lang.Object@(X))' in 'foo.I0@(X)', attempting to use incompatible return type

}

interface I0@A {
	void m (Object@A y);
}

interface I1@A {
	<T@X> void m (Object@A y);
}

interface I2@A {
	<T@X> void m (T@A y);
}

interface I3@A {
	String@A m (Object@A y);
}
