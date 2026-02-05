package choral.MustPass.MoveMeant.Mergesort;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;
import java.util.List;

@Choreography( role = "C", name = "Mergesort" )
public class Mergesort_C {
	SymChannel_B < Object > ch_BC;
	SymChannel_A < Object > ch_CA;

	public Mergesort_C( Unit ch_AB, SymChannel_B < Object > ch_BC, SymChannel_A < Object > ch_CA ) {
		this( ch_BC, ch_CA );
	}
	
	public Mergesort_C( SymChannel_B < Object > ch_BC, SymChannel_A < Object > ch_CA ) {
		this.ch_BC = ch_BC;
		this.ch_CA = ch_CA;
	}

	public Unit sort( Unit a ) {
		return sort();
	}
	
	private Unit merge( Unit lhs, List < Integer > rhs ) {
		switch( ch_BC.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				if( rhs.size() > 0 ){
					ch_BC.< KOCEnum >select( KOCEnum.CASE0 );
					ch_CA.< KOCEnum >select( KOCEnum.CASE0 );
					ch_BC.< Integer >com( rhs.get( 0 ) );
					switch( ch_BC.< KOCEnum >select( Unit.id ) ){
						case CASE0 -> {
							merge( Unit.id, rhs );
							return Unit.id;
						}
						case CASE1 -> {
							ch_CA.< Integer >com( rhs.get( 0 ) );
							merge( Unit.id, rhs.subList( 1, rhs.size() ) );
							return Unit.id;
						}
						default -> {
							throw new RuntimeException( "Received unexpected label from select operation" );
						}
					}
				} else { 
					ch_BC.< KOCEnum >select( KOCEnum.CASE1 );
					ch_CA.< KOCEnum >select( KOCEnum.CASE1 );
					return Unit.id;
				}
			}
			case CASE1 -> {
				ch_CA.< List < Integer > >com( rhs );
				return Unit.id;
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}
	
	public Unit sort() {
		switch( ch_CA.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				Mergesort_B mb = new Mergesort_B( ch_BC, ch_CA, Unit.id );
				Mergesort_A mc = new Mergesort_A( ch_CA, Unit.id, ch_BC );
				mb.sort( Unit.id );
				List < Integer > msg1 = ch_CA.< List < Integer > >com( Unit.id );
				List < Integer > rhs = mc.sort( msg1 );
				return merge( Unit.id, rhs );
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
