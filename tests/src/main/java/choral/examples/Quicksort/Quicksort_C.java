package choral.examples.Quicksort;

import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import java.util.List;
import choral.lang.Unit;
import java.util.ArrayList;
import choral.annotations.Choreography;

@Choreography( role = "C", name = "Quicksort" )
public class Quicksort_C {
	SymChannel_B < Object > ch_BC;
	SymChannel_A < Object > ch_CA;

	public Quicksort_C( Unit ch_AB, SymChannel_B < Object > ch_BC, SymChannel_A < Object > ch_CA ) {
		this( ch_BC, ch_CA );
	}

	public Quicksort_C( SymChannel_B < Object > ch_BC, SymChannel_A < Object > ch_CA ) {
		this.ch_BC = ch_BC;
		this.ch_CA = ch_CA;
	}

	public Unit sort( Unit a ) {
		return sort();
	}

	private void partition( Unit a, Unit pivot, Unit greater, List < Integer > lower ) {
		{
			switch( ch_CA.< Loop >select( Unit.id ) ){
				case STOP -> {

				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case GO -> {
					{
						switch( ch_CA.< Recv >select( Unit.id ) ){
							case B -> {

							}
							case C -> {
								lower.add( ch_CA.< Integer >com( Unit.id ) );
							}
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
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
				case STOP -> {
					return Unit.id;
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case GO -> {
					List < Integer > lowerPartition = new ArrayList < Integer >();
					partition( Unit.id, Unit.id, Unit.id, lowerPartition );
					Quicksort_A qc = new Quicksort_A( ch_CA, Unit.id, ch_BC );
					Quicksort_B qb = new Quicksort_B( ch_BC, ch_CA, Unit.id );
					ch_CA.< List < Integer > >com( qc.sort( lowerPartition ) );
					qb.sort( Unit.id );
					return Unit.id;
				}
			}
		}
	}

}
