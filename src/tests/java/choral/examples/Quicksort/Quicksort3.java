package choral.examples.Quicksort;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import java.util.List;
import java.util.ArrayList;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "C", name = "Quicksort" )
public class Quicksort3 {
	SymChannel2 < Object > ch_BC;
	SymChannel1 < Object > ch_CA;

	public Quicksort3( Unit ch_AB, SymChannel2 < Object > ch_BC, SymChannel1 < Object > ch_CA ) {
		this( ch_BC, ch_CA );
	}

	public Quicksort3( SymChannel2 < Object > ch_BC, SymChannel1 < Object > ch_CA ) {
		this.ch_BC = ch_BC;
		this.ch_CA = ch_CA;
	}

	public Unit sort( Unit a ) {
		return sort();
	}

	private void partition( Unit a, Unit pivot, Unit greater, List < Integer > lower ) {
		{
			switch( ch_CA.< Loop >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case STOP -> {

				}
				case GO -> {
					{
						switch( ch_CA.< Recv >select( Unit.id ) ){
							case B -> {

							}
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
							}
							case C -> {
								lower.add( ch_CA.< Integer >com( Unit.id ) );
							}
						}
					}
					partition( Unit.id, Unit.id, Unit.id, lower );
				}
			}
		}
	}

	public Unit sort() {
		{
			switch( ch_CA.< Loop >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case STOP -> {
					return Unit.id;
				}
				case GO -> {
					List < Integer > lowerPartition;
					lowerPartition = new ArrayList< Integer >();
					partition( Unit.id, Unit.id, Unit.id, lowerPartition );
					Quicksort1 qc;
					qc = new Quicksort1( ch_CA, Unit.id, ch_BC );
					Quicksort2 qb;
					qb = new Quicksort2( ch_BC, ch_CA, Unit.id );
					ch_CA.< List < Integer > >com( qc.sort( lowerPartition ) );
					qb.sort( Unit.id );
					return Unit.id;
				}
			}
		}
	}

}
