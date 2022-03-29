package choral.examples.Mergesort;

import java.util.List;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

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
			ch_AB.< MChoice >select( MChoice.L );
			ch_BC.< MChoice >select( MChoice.L );
			{
				switch( ch_BC.< MChoice >select( Unit.id ) ){
					case R -> {
						return ch_AB.< List < Integer > >com( lhs );
					}
					default -> {
						throw new RuntimeException( "Received unexpected label from select operation" );
					}
					case L -> {
						if( lhs.get( 0 ) <= ch_BC.< Integer >com( Unit.id ) ){
							ch_AB.< MChoice >select( MChoice.L );
							ch_BC.< MChoice >select( MChoice.L );
							ch_AB.< Integer >com( lhs.get( 0 ) );
							merge( lhs.subList( 1, lhs.size() ), Unit.id );
							return Unit.id;
						} else { 
							ch_AB.< MChoice >select( MChoice.R );
							ch_BC.< MChoice >select( MChoice.R );
							merge( lhs, Unit.id );
							return Unit.id;
						}
					}
				}
			}
		} else { 
			ch_AB.< MChoice >select( MChoice.R );
			ch_BC.< MChoice >select( MChoice.R );
			return Unit.id;
		}
	}
	
	public Unit sort() {
		{
			switch( ch_AB.< MChoice >select( Unit.id ) ){
				case R -> {
					return Unit.id;
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case L -> {
					Mergesort_A mb = new Mergesort_A( ch_BC, Unit.id, ch_AB );
					Mergesort_C mc = new Mergesort_C( Unit.id, ch_AB, ch_BC );
					List < Integer > lhs = mb.sort( ch_AB.< List < Integer > >com( Unit.id.id( Unit.id, Unit.id.id() ) ) );
					mc.sort( Unit.id );
					return merge( lhs, Unit.id );
				}
			}
		}
	}

}
