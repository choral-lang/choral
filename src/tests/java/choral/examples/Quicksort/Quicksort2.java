package choral.examples.Quicksort;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import java.util.List;
import java.util.ArrayList;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "B", name = "Quicksort" )
public class Quicksort2 {
	SymChannel2 < Object > ch_AB;
	SymChannel1 < Object > ch_BC;

	public Quicksort2( SymChannel2 < Object > ch_AB, SymChannel1 < Object > ch_BC, Unit ch_CA ) {
		this( ch_AB, ch_BC );
	}

	public Quicksort2( SymChannel2 < Object > ch_AB, SymChannel1 < Object > ch_BC ) {
		this.ch_AB = ch_AB;
		this.ch_BC = ch_BC;
	}

	public Unit sort( Unit a ) {
		return sort();
	}

	private void partition( Unit a, Unit pivot, List < Integer > greater, Unit lower ) {
		{
			switch( ch_AB.< Loop >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case STOP -> {

				}
				case GO -> {
					{
						switch( ch_AB.< Recv >select( Unit.id ) ){
							case B -> {
								greater.add( ch_AB.< Integer >com( Unit.id ) );
							}
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
							}
							case C -> {

							}
						}
					}
					partition( Unit.id, Unit.id, greater, Unit.id );
				}
			}
		}
	}

	public Unit sort() {
		{
			switch( ch_AB.< Loop >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case STOP -> {
					return Unit.id;
				}
				case GO -> {
					List < Integer > greaterPartition;
					greaterPartition = new ArrayList< Integer >();
					partition( Unit.id, Unit.id, greaterPartition, Unit.id );
					Quicksort3 qc;
					qc = new Quicksort3( Unit.id, ch_AB, ch_BC );
					Quicksort1 qb;
					qb = new Quicksort1( ch_BC, Unit.id, ch_AB );
					qc.sort( Unit.id );
					ch_AB.< List < Integer > >com( qb.sort( greaterPartition ) );
					return Unit.id;
				}
			}
		}
	}

}
