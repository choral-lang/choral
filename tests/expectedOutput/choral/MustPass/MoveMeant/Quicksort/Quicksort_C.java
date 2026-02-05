package choral.MustPass.MoveMeant.Quicksort;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;
import java.util.ArrayList;
import java.util.List;

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
		switch( ch_CA.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				switch( ch_CA.< KOCEnum >select( Unit.id ) ){
					case CASE0 -> {
						
					}
					case CASE1 -> {
						Integer dependencyAtC_82567285 = ch_CA.< Integer >com( Unit.id );
						lower.add( dependencyAtC_82567285 );
					}
					default -> {
						throw new RuntimeException( "Received unexpected label from select operation" );
					}
				}
				partition( Unit.id, Unit.id, Unit.id, lower );
			}
			case CASE1 -> {
				
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}
	
	public Unit sort() {
		switch( ch_CA.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				List < Integer > lowerPartition = new ArrayList < Integer >();
				partition( Unit.id, Unit.id, Unit.id, lowerPartition );
				Quicksort_A qc = new Quicksort_A( ch_CA, Unit.id, ch_BC );
				Quicksort_B qb = new Quicksort_B( ch_BC, ch_CA, Unit.id );
				ch_CA.< List < Integer > >com( qc.sort( lowerPartition ) );
				qb.sort( Unit.id );
				return Unit.id;
			}
			case CASE1 -> {
				return Unit.id;
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
