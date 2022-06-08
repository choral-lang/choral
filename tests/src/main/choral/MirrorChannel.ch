import choral.channels.DiChannel;
import choral.channels.SymChannel;

public class MirrorChannel@(A,B)<T@A> implements SymChannel@(A,B)<T>, SymChannel@(B,A)<T> {

	private final DiChannel@(A,B)<T> ab;
	private final DiChannel@(B,A)<T> ba;
	private final MirrorChannel@(B,A)<T> mc;


	private MirrorChannel(MirrorChannel@(B,A)<T> mc) {
		this.mc = mc;
		this.ab = mc.ba;
		this.ba = mc.ab;
	}

	public MirrorChannel(DiChannel@(A,B)<T> ab, DiChannel@(B,A)<T> ba) {
		this.ab = ab;
		this.ba = ba;
		this.mc = new MirrorChannel@(B,A)<T>(this);
	}

	public <S@A extends T@A> S@B com( S@A m ) {
		return this.ab.<S>com(m);
	}

	public <S@A extends T@A> S@A com( S@B m ) {
		return this.ba.<S>com(m);
	}

	public <T@A extends Enum@A<T>> T@B select( T@A m ) {
		return this.ab.<T>select(m);
	}

	public <T@A extends Enum@A<T>> T@A select( T@B m ) {
		return this.ba.<T>select(m);
	}

	public MirrorChannel@(B,A)<T> flip(){
		return this.mc;
	}

}