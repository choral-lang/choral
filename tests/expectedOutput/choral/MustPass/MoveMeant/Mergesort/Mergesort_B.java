package choral.MustPass.MoveMeant.Mergesort;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;
import java.util.List;

@Choreography( role = "B", name = "Mergesort" )
public class Mergesort_B {
	SymChannel_B < Object > ch_AB;
	SymChannel_A < Object > ch_BC;

	public Mergesort_B( SymChannel_B < Object > ch_AB, SymChannel_A < Object > ch_BC, Unit ch_CA ) {
		this( ch_AB, ch_BC );
	}
	
	public Mergesort_B( SymChannel_B < Object > ch_AB, SymChannel_A < Object > ch_BC ) {
		this.ch_AB = ch_AB;
		this.ch_BC = ch_BC;
	}

	public Unit sort( Unit a ) {
		return sort();
	}
	
	private Unit merge( List < Integer > lhs, Unit rhs ) {
		if( lhs.size() > 0 ){
			ch_BC.< KOCEnum >select( KOCEnum.CASE0 );
			ch_AB.< KOCEnum >select( KOCEnum.CASE0 );
			switch( ch_BC.< KOCEnum >select( Unit.id ) ){
				case CASE0 -> {
					Integer msg2 = ch_BC.< Integer >com( Unit.id );
					if( lhs.get( 0 ) <= msg2 ){
						ch_BC.< KOCEnum >select( KOCEnum.CASE0 );
						ch_AB.< KOCEnum >select( KOCEnum.CASE0 );
						ch_AB.< Integer >com( lhs.get( 0 ) );
						merge( lhs.subList( 1, lhs.size() ), Unit.id );
						return Unit.id;
					} else { 
						ch_BC.< KOCEnum >select( KOCEnum.CASE1 );
						ch_AB.< KOCEnum >select( KOCEnum.CASE1 );
						merge( lhs, Unit.id );
						return Unit.id;
					}
				}
				case CASE1 -> {
					ch_AB.< List < Integer > >com( lhs );
					return Unit.id;
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		} else { 
			ch_BC.< KOCEnum >select( KOCEnum.CASE1 );
			ch_AB.< KOCEnum >select( KOCEnum.CASE1 );
			return Unit.id;
		}
	}
	
	public Unit sort() {
		switch( ch_AB.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				Mergesort_A mb = new Mergesort_A( ch_BC, Unit.id, ch_AB );
				Mergesort_C mc = new Mergesort_C( Unit.id, ch_AB, ch_BC );
				List < Integer > msg0 = ch_AB.< List < Integer > >com( Unit.id );
				List < Integer > lhs = mb.sort( msg0 );
				mc.sort( Unit.id );
				return merge( lhs, Unit.id );
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
