package choral.examples.MultiFoo;
import org.choral.lang.Unit;

public class MultiFoo_F extends SingleFoo_F {
	Bar_D < Foo_A, Foo_B > x;

	public MultiFoo_F( SingleFoo_F y ) {
		super( staticMethod() );
	}

}
