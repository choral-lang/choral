package choral.MustPass.MoveMeant.Mergesort;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;
import java.util.ArrayList;
import java.util.List;

@Choreography( role = "A", name = "Mergesort" )
public class Mergesort_A {
	SymChannel_A < Object > ch_AB;
	SymChannel_B < Object > ch_CA;

	public Mergesort_A( SymChannel_A < Object > ch_AB, Unit ch_BC, SymChannel_B < Object > ch_CA ) {
		this( ch_AB, ch_CA );
	}
	
	public Mergesort_A( SymChannel_A < Object > ch_AB, SymChannel_B < Object > ch_CA ) {
		this.ch_AB = ch_AB;
		this.ch_CA = ch_CA;
	}

	public List < Integer > sort( List < Integer > a ) {
		if( a.size() > 1 ){
			ch_AB.< KOCEnum >select( KOCEnum.CASE0 );
			ch_CA.< KOCEnum >select( KOCEnum.CASE0 );
			Mergesort_C mb = new Mergesort_C( Unit.id, ch_CA, ch_AB );
			Mergesort_B mc = new Mergesort_B( ch_CA, ch_AB, Unit.id );
			Double pivot = Double.valueOf( Math.floor( a.size() / 2 ) );
			ch_AB.< List < Integer > >com( a.subList( 0, pivot.intValue() ) );
			mb.sort( Unit.id );
			ch_CA.< List < Integer > >com( a.subList( pivot.intValue(), a.size() ) );
			mc.sort( Unit.id );
			return merge( Unit.id, Unit.id );
		} else { 
			ch_AB.< KOCEnum >select( KOCEnum.CASE1 );
			ch_CA.< KOCEnum >select( KOCEnum.CASE1 );
			return a;
		}
	}
	
	private List < Integer > merge( Unit lhs, Unit rhs ) {
		switch( ch_AB.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				switch( ch_CA.< KOCEnum >select( Unit.id ) ){
					case CASE0 -> {
						ArrayList < Integer > result = new ArrayList < Integer >();
						switch( ch_AB.< KOCEnum >select( Unit.id ) ){
							case CASE0 -> {
								Integer msg3 = ch_AB.< Integer >com( Unit.id );
								result.add( msg3 );
								result.addAll( merge( Unit.id, Unit.id ) );
								return result;
							}
							case CASE1 -> {
								Integer msg4 = ch_CA.< Integer >com( Unit.id );
								result.add( msg4 );
								result.addAll( merge( Unit.id, Unit.id ) );
								return result;
							}
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
							}
						}
					}
					case CASE1 -> {
						List < Integer > msg5 = ch_AB.< List < Integer > >com( Unit.id );
						return msg5;
					}
					default -> {
						throw new RuntimeException( "Received unexpected label from select operation" );
					}
				}
			}
			case CASE1 -> {
				List < Integer > msg6 = ch_CA.< List < Integer > >com( Unit.id );
				return msg6;
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
