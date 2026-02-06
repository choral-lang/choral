package choral.MustPass.MoveMeant.Quicksort;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;
import java.util.ArrayList;
import java.util.List;

@Choreography( role = "A", name = "Quicksort" )
public class Quicksort_A {
	SymChannel_A < Object > ch_AB;
	SymChannel_B < Object > ch_CA;

	public Quicksort_A( SymChannel_A < Object > ch_AB, Unit ch_BC, SymChannel_B < Object > ch_CA ) {
		this( ch_AB, ch_CA );
	}
	
	public Quicksort_A( SymChannel_A < Object > ch_AB, SymChannel_B < Object > ch_CA ) {
		this.ch_AB = ch_AB;
		this.ch_CA = ch_CA;
	}

	public List < Integer > sort( List < Integer > a ) {
		if( a.size() > 1 ){
			ch_CA.< KOCEnum >select( KOCEnum.CASE0 );
			ch_AB.< KOCEnum >select( KOCEnum.CASE0 );
			Double index = Double.valueOf( Math.floor( a.size() / 2 ) );
			Integer pivot = a.remove( index.intValue() );
			partition( a, pivot, Unit.id, Unit.id );
			Quicksort_B qc = new Quicksort_B( ch_CA, ch_AB, Unit.id );
			Quicksort_C qb = new Quicksort_C( Unit.id, ch_CA, ch_AB );
			List < Integer > msg0 = ch_CA.< List < Integer > >com( qc.sort( Unit.id ) );
			List < Integer > orderedList = new ArrayList < Integer >( msg0 );
			orderedList.add( pivot );
			List < Integer > msg1 = ch_AB.< List < Integer > >com( qb.sort( Unit.id ) );
			orderedList.addAll( msg1 );
			return orderedList;
		} else { 
			ch_CA.< KOCEnum >select( KOCEnum.CASE1 );
			ch_AB.< KOCEnum >select( KOCEnum.CASE1 );
			return a;
		}
	}
	
	private void partition( List < Integer > a, Integer pivot, Unit greater, Unit lower ) {
		if( a.size() > 0 ){
			ch_CA.< KOCEnum >select( KOCEnum.CASE0 );
			ch_AB.< KOCEnum >select( KOCEnum.CASE0 );
			Integer i = a.remove( 0 );
			if( i > pivot ){
				ch_CA.< KOCEnum >select( KOCEnum.CASE0 );
				ch_AB.< KOCEnum >select( KOCEnum.CASE0 );
				ch_AB.< Integer >com( i );
			} else { 
				ch_CA.< KOCEnum >select( KOCEnum.CASE1 );
				ch_AB.< KOCEnum >select( KOCEnum.CASE1 );
				ch_CA.< Integer >com( i );
			}
			partition( a, pivot, Unit.id, Unit.id );
		} else { 
			ch_CA.< KOCEnum >select( KOCEnum.CASE1 );
			ch_AB.< KOCEnum >select( KOCEnum.CASE1 );
		}
	}

}
