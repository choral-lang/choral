package choral.examples.Quicksort;

import choral.channels.SymChannel_A;
import choral.lang.Unit;

import java.util.List;

import choral.channels.SymChannel_B;

import java.util.ArrayList;

import choral.annotations.Choreography;

@Choreography( role = "B", name = "Quicksort" )
public class Quicksort_B {
	SymChannel_B< Object > ch_AB;
	SymChannel_A< Object > ch_BC;

	public Quicksort_B( SymChannel_B< Object > ch_AB, SymChannel_A< Object > ch_BC, Unit ch_CA ) {
		this( ch_AB, ch_BC );
	}

	public Quicksort_B( SymChannel_B< Object > ch_AB, SymChannel_A< Object > ch_BC ) {
		this.ch_AB = ch_AB;
		this.ch_BC = ch_BC;
	}

	public Unit sort( Unit a ) {
		return sort();
	}

	private void partition( Unit a, Unit pivot, List< Integer > greater, Unit lower ) {
		{
			switch( ch_AB.< Loop >select( Unit.id ) ) {
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case STOP -> {

				}
				case GO -> {
					{
						switch( ch_AB.< Recv >select( Unit.id ) ) {
							case B -> {
								greater.add( ch_AB.< Integer >com( Unit.id ) );
							}
							default -> {
								throw new RuntimeException(
										"Received unexpected label from select operation" );
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
			switch( ch_AB.< Loop >select( Unit.id ) ) {
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case STOP -> {
					return Unit.id;
				}
				case GO -> {
					List< Integer > greaterPartition;
					greaterPartition = new ArrayList< Integer >();
					partition( Unit.id, Unit.id, greaterPartition, Unit.id );
					Quicksort_C qc;
					qc = new Quicksort_C( Unit.id, ch_AB, ch_BC );
					Quicksort_A qb;
					qb = new Quicksort_A( ch_BC, Unit.id, ch_AB );
					qc.sort( Unit.id );
					ch_AB.< List< Integer > >com( qb.sort( greaterPartition ) );
					return Unit.id;
				}
			}
		}
	}

}
