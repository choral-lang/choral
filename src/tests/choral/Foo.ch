package foo;

class C1@A implements I1@A, I2@A {

	@Override
	public void m( Object@A x, Object@A y ) {
		new C1@A().<Integer>m( x,5@A );
	}

}

interface I1@A {
	void m (Object@A x, Object@A y);
}

interface I2@A {
	<T@X> void m (Object@A x, T@A y);
}