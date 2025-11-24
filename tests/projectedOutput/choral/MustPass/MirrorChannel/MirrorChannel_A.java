package choral.MustPass.MirrorChannel;

import choral.annotations.Choreography;
import choral.channels.DiChannel_A;
import choral.channels.DiChannel_B;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "A", name = "MirrorChannel" )
public class MirrorChannel_A< T > implements SymChannel_A < T >,SymChannel_B < T > {
	private final DiChannel_A < T > ab;
	private final DiChannel_B < T > ba;
	private final MirrorChannel_B < T > mc;

	private MirrorChannel_A( MirrorChannel_B < T > mc ) {
		this.mc = mc;
		this.ab = mc.ba;
		this.ba = mc.ab;
	}
	
	public MirrorChannel_A( DiChannel_A < T > ab, DiChannel_B < T > ba ) {
		this.ab = ab;
		this.ba = ba;
		this.mc = new MirrorChannel_B < T >( this );
	}

	public < S extends T > Unit com( S m ) {
		return this.ab.< S >com( m );
	}
	
	public < S extends T > S com( Unit m ) {
		return < S >com();
	}
	
	public < T extends Enum < T > > Unit select( T m ) {
		return this.ab.< T >select( m );
	}
	
	public < T extends Enum < T > > T select( Unit m ) {
		return < T >select();
	}
	
	public MirrorChannel_B < T > flip() {
		return this.mc;
	}
	
	public < S extends T > S com() {
		return this.ba.< S >com( Unit.id );
	}
	
	public < T extends Enum < T > > T select() {
		return this.ba.< T >select( Unit.id );
	}

}
