package choral.examples.Quicksort;

import choral.channels.SymChannel;
import java.util.List;
import java.util.ArrayList;
import choral.runtime.Serializers.KryoSerializable;

enum Loop@R{ STOP, GO }
enum Recv@R{ B, C }

public class Quicksort@( A, B, C ){
	SymChannel@( A, B )< Object > ch_AB;
	SymChannel@( B, C )< Object > ch_BC;
	SymChannel@( C, A )< Object > ch_CA;

	public Quicksort(
		SymChannel@( A, B )< Object > ch_AB,
		SymChannel@( B, C )< Object > ch_BC,
		SymChannel@( C, A )< Object > ch_CA
	){
		this.ch_AB = ch_AB;
		this.ch_BC = ch_BC;
		this.ch_CA = ch_CA;
	}

	public List@A< Integer > sort ( List@A< Integer > a ){
			if( a.size() > 1@A ){
				ch_AB.< Loop >select( Loop@A.GO );
				ch_CA.< Loop >select( Loop@A.GO );
				Double@A index = a.size() / 2@A
					>> Math@A::floor
					>> Double@A::valueOf;
				Integer@A pivot = index.intValue() >> a::remove;
				List@B< Integer > greaterPartition = new ArrayList@B< Integer >();
				List@C< Integer > lowerPartition = new ArrayList@C< Integer >();
				partition( a, pivot, greaterPartition, lowerPartition );
				Quicksort@( C, A, B ) qc = new Quicksort@( C, A, B )( ch_CA, ch_AB, ch_BC );
				Quicksort@( B, C, A ) qb = new Quicksort@( B, C, A )( ch_BC, ch_CA, ch_AB );
				List@A< Integer > orderedList =
				lowerPartition
					>> qc::sort
					>> ch_CA::< List < Integer > >com
					>> ArrayList@A< Integer >::new;
				pivot >> orderedList::add;
				greaterPartition
					>> qb::sort
					>> ch_AB::< List< Integer > >com
					>> orderedList::addAll;
				return orderedList;
			} else {
				ch_AB.< Loop >select( Loop@A.STOP );
				ch_CA.< Loop >select( Loop@A.STOP );
				return a;
			}
		}

		private void partition(
				List@A< Integer > a, Integer@A pivot,
				List@B< Integer > greater, List@C< Integer > lower
		){
			if( a.size() > 0@A ){
				ch_AB.< Loop >select( Loop@A.GO );
				ch_CA.< Loop >select( Loop@A.GO );
				Integer@A i = a.remove( 0@A );
				if ( i > pivot ){
					ch_AB.< Recv >select( Recv@A.B );
					ch_CA.< Recv >select( Recv@A.B );
					i >> ch_AB::< Integer >com >> greater::add;
				} else {
					ch_AB.< Recv >select( Recv@A.C );
					ch_CA.< Recv >select( Recv@A.C );
					i >> ch_CA::< Integer >com >> lower::add;
				}
				partition( a, pivot, greater, lower );
			} else {
				ch_AB.< Loop >select( Loop@A.STOP );
				ch_CA.< Loop >select( Loop@A.STOP );
			}
		}
}
