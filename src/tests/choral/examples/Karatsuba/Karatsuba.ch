package choral.examples.Karatsuba;

import org.choral.lang.Channels.SymChannel;
import org.choral.lang.Unit;

public enum Choice@R { RECUR, DONE }

public class Karatsuba@( A, B, C ) {

	public static Long@A multiply ( Long@A n1, Long@A n2,
		SymChannel@( A, B )< Object > ch_AB,
		SymChannel@( B, C )< Object > ch_BC,
		SymChannel@( C, A )< Object > ch_CA ) {
		if ( n1 < 10@A || n2 < 10@A ) {
			select( Choice@A.DONE, ch_AB ); select( Choice@A.DONE, ch_CA );
			return n1 * n2;
		} else {
			select( Choice@A.RECUR, ch_AB ); select( Choice@A.RECUR, ch_CA );
			Double@A m = Math@A.max( Math@A.log10( n1 ), Math@A.log10( n2 ) ) + 1@A;
			Integer@A m2 = Double@A.valueOf( m / 2@A ).intValue();
			Integer@A splitter = Double@A.valueOf( Math@A.pow( 10@A, m2 ) ).intValue();
			Long@A h1 = n1 / splitter; Long@A l1 = n1 % splitter;
			Long@A h2 = n2 / splitter; Long@A l2 = n2 % splitter;
			Long@A z0 = Karatsuba@( B, C, A )
				.multiply( ch_AB.< Long >com( l1 ), ch_AB.< Long >com( l2 ), ch_BC, ch_CA, ch_AB )
				>> ch_AB::< Long >com;
			Long@A z2 = Karatsuba@( C, A, B )
				.multiply( ch_CA.< Long >com( h1 ), ch_CA.< Long >com( h2 ), ch_CA, ch_AB, ch_BC )
				>> ch_CA::< Long >com;
			Long@A z1 = Karatsuba@( A, B, C ).multiply( l1 + h1, l2 + h2, ch_AB, ch_BC, ch_CA ) - z2 - z0;
			return z2 * splitter * splitter + z1 * splitter + z0;
		}
	}
}
