package choral.examples.Karatsuba;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "C", name = "Karatsuba" )
public class Karatsuba3 {
	public static Unit multiply( Unit n1, Unit n2, Unit ch_AB, SymChannel2 < Object > ch_BC, SymChannel1 < Object > ch_CA ) {
		return multiply( ch_BC, ch_CA );
	}

	public static Unit multiply( SymChannel2 < Object > ch_BC, SymChannel1 < Object > ch_CA ) {
		{
			switch( ch_CA.< Choice >select( Unit.id ) ){
				case DONE -> {
					return Unit.id;
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case RECUR -> {
					Karatsuba2.multiply( Unit.id, Unit.id, ch_BC, ch_CA, Unit.id );
					ch_CA.< Long >com( Karatsuba1.multiply( ch_CA.< Long >com( Unit.id ), ch_CA.< Long >com( Unit.id ), ch_CA, Unit.id, ch_BC ) );
					Karatsuba3.multiply( Unit.id, Unit.id, Unit.id, ch_BC, ch_CA );
					return Unit.id;
				}
			}
		}
	}

}
