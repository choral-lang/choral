package choral.examples.Karatsuba;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "A", name = "Karatsuba" )
public class Karatsuba1 {
	public static Long multiply( Long n1, Long n2, SymChannel1 < Object > ch_AB, Unit ch_BC, SymChannel2 < Object > ch_CA ) {
		return multiply( n1, n2, ch_AB, ch_CA );
	}

	public static Long multiply( Long n1, Long n2, SymChannel1 < Object > ch_AB, SymChannel2 < Object > ch_CA ) {
		if( n1 < 10 || n2 < 10 ){
			ch_AB.< Choice >select( Choice.DONE );
			ch_CA.< Choice >select( Choice.DONE );
			return n1 * n2;
		} else {
			ch_AB.< Choice >select( Choice.RECUR );
			ch_CA.< Choice >select( Choice.RECUR );
			Double m;
			m = Math.max( Math.log10( n1 ), Math.log10( n2 ) ) + 1;
			Integer m2;
			m2 = Double.valueOf( m / 2 ).intValue();
			Integer splitter;
			splitter = Double.valueOf( Math.pow( 10, m2 ) ).intValue();
			Long h1;
			h1 = n1 / splitter;
			Long l1;
			l1 = n1 % splitter;
			Long h2;
			h2 = n2 / splitter;
			Long l2;
			l2 = n2 % splitter;
			Long z0;
			z0 = ch_AB.< Long >com( Karatsuba3.multiply( ch_AB.< Long >com( l1 ), ch_AB.< Long >com( l2 ), Unit.id, ch_CA, ch_AB ) );
			Long z2;
			z2 = ch_CA.< Long >com( Karatsuba2.multiply( ch_CA.< Long >com( h1 ), ch_CA.< Long >com( h2 ), ch_CA, ch_AB, Unit.id ) );
			Long z1;
			z1 = Karatsuba1.multiply( l1 + h1, l2 + h2, ch_AB, Unit.id, ch_CA ) - z2 - z0;
			return z2 * splitter * splitter + z1 * splitter + z0;
		}
	}

}
