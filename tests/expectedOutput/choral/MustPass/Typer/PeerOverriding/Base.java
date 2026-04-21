package choral.MustPass.Typer.PeerOverriding;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Base" )
abstract class Base< T > {
	public abstract void process( T input );
	
	public void process( String input ) {
		
	}

}
