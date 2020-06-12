package choral.examples.BiPair;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "A", name = "BiPair" )
public class BiPair1< L, R > {
	private L left;

	public BiPair1( L left, Unit right ) {
		this( left );
	}

	public BiPair1( L left ) {
		this.left = left;
	}

	public L left() {
		return this.left;
	}

	public Unit right() {
		return Unit.id;
	}

}
