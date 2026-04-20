package choral.MustPass.Typer.OnDemandImports;

import choral.annotations.Choreography;
import java.net.*;

@Choreography( role = "A", name = "OnDemandImports" )
class OnDemandImports_A {
	public void test() {
		URI time = new URI( "https://example.com/search?q=hello%20world" );
	}

}
