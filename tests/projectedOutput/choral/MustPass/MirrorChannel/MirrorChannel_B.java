package choral.MustPass.MirrorChannel;

import choral.annotations.Choreography;
import choral.channels.DiChannel_A;
import choral.channels.DiChannel_B;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "MirrorChannel" )
public class MirrorChannel_B< T > implements SymChannel_B < T >,SymChannel_A < T > {
	private final DiChannel_B < T > ab;
	private final DiChannel_A < T > ba;
	private final MirrorChannel_A < T > mc;

	private MirrorChannel_B( MirrorChannel_A < T > mc ) {
		this.mc = mc;
		this.ab = mc.ba;
		this.ba = mc.ab;
	}
	
	public MirrorChannel_B( DiChannel_B < T > ab, DiChannel_A < T > ba ) {
		this.ab = ab;
		this.ba = ba;
		this.mc = new MirrorChannel_A < T >( this );
	}

	public < S extends T > S com( Unit m ) {
		return < S >com();
	}
	
	public < S extends T > Unit com( S m ) {
		return this.ba.< S >com( m );
	}
	
	public < T extends Enum < T > > T select( Unit m ) {
		return < T >select();
	}
	
	public < T extends Enum < T > > Unit select( T m ) {
		return this.ba.< T >select( m );
	}
	
	public MirrorChannel_A < T > flip() {
		return this.mc;
	}
	
	public < S extends T > S com() {
		return this.ab.< S >com( Unit.id );
	}
	
	public < T extends Enum < T > > T select() {
		return this.ab.< T >select( Unit.id );
	}

}
