package choral.examples.Karatsuba;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "B", name = "Karatsuba" )
public class Karatsuba2 {
	public static Unit multiply( Unit n1, Unit n2, SymChannel2 < Object > ch_AB, SymChannel1 < Object > ch_BC, Unit ch_CA ) {
		return multiply( ch_AB, ch_BC );
	}

	public static Unit multiply( SymChannel2 < Object > ch_AB, SymChannel1 < Object > ch_BC ) {
		{
			switch( ch_AB.< Choice >select( Unit.id ) ){
				case DONE -> {
					return Unit.id;
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case RECUR -> {
					ch_AB.< Long >com( Karatsuba1.multiply( ch_AB.< Long >com( Unit.id ), ch_AB.< Long >com( Unit.id ), ch_BC, Unit.id, ch_AB ) );
					Karatsuba3.multiply( Unit.id, Unit.id, Unit.id, ch_AB, ch_BC );
					Karatsuba2.multiply( Unit.id, Unit.id, ch_AB, ch_BC, Unit.id );
					return Unit.id;
				}
			}
		}
	}

}
