package choral.examples.MultiFoo;

import choral.lang.Unit;

public class MultiFoo_E extends SingleFoo_E {
	Bar_C< Foo_A, Foo_B > x;

	public MultiFoo_E( SingleFoo_E y ) {
		super( staticMethod() );
	}

}
