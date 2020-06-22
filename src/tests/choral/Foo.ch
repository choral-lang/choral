package foo;

class C1@A implements I1@A, I2@A {

	@Override
	public void m (Object@A y) {
	    Integer@A z = 5@A;
		new C1@A().<Integer>m( z );
	}

}

// fails
class C2@A implements I1@A, I2@A {
/*
    public <T@X> void m (Object@A y) {
	    Integer@A z = 5@A;
		new C2@A().<Integer>m( z );
	}
*/
}

interface I1@A {
	void m (Object@A y);
}

interface I2@A {
	<T@X> void m (T@A y);
}