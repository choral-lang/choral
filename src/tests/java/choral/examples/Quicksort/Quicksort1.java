package choral.examples.Quicksort;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import java.util.List;
import java.util.ArrayList;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "A", name = "Quicksort" )
public class Quicksort1 {
	SymChannel1 < Object > ch_AB;
	SymChannel2 < Object > ch_CA;

	public Quicksort1( SymChannel1 < Object > ch_AB, Unit ch_BC, SymChannel2 < Object > ch_CA ) {
		this( ch_AB, ch_CA );
	}

	public Quicksort1( SymChannel1 < Object > ch_AB, SymChannel2 < Object > ch_CA ) {
		this.ch_AB = ch_AB;
		this.ch_CA = ch_CA;
	}

	public List < Integer > sort( List < Integer > a ) {
		if( a.size() > 1 ){
			ch_AB.< Loop >select( Loop.GO );
			ch_CA.< Loop >select( Loop.GO );
			Double index;
			index = Double.valueOf( Math.floor( a.size() / 2 ) );
			Integer pivot;
			pivot = a.remove( index.intValue() );
			partition( a, pivot, Unit.id, Unit.id );
			Quicksort2 qc;
			qc = new Quicksort2( ch_CA, ch_AB, Unit.id );
			Quicksort3 qb;
			qb = new Quicksort3( Unit.id, ch_CA, ch_AB );
			List < Integer > orderedList;
			orderedList = new ArrayList< Integer >( ch_CA.< List < Integer > >com( qc.sort( Unit.id ) ) );
			orderedList.add( pivot );
			orderedList.addAll( ch_AB.< List < Integer > >com( qb.sort( Unit.id ) ) );
			return orderedList;
		} else {
			ch_AB.< Loop >select( Loop.STOP );
			ch_CA.< Loop >select( Loop.STOP );
			return a;
		}
	}

	private void partition( List < Integer > a, Integer pivot, Unit greater, Unit lower ) {
		if( a.size() > 0 ){
			ch_AB.< Loop >select( Loop.GO );
			ch_CA.< Loop >select( Loop.GO );
			Integer i;
			i = a.remove( 0 );
			if( i > pivot ){
				ch_AB.< Recv >select( Recv.B );
				ch_CA.< Recv >select( Recv.B );
				ch_AB.< Integer >com( i );
			} else {
				ch_AB.< Recv >select( Recv.C );
				ch_CA.< Recv >select( Recv.C );
				ch_CA.< Integer >com( i );
			}
			partition( a, pivot, Unit.id, Unit.id );
		} else {
			ch_AB.< Loop >select( Loop.STOP );
			ch_CA.< Loop >select( Loop.STOP );
		}
	}

}
