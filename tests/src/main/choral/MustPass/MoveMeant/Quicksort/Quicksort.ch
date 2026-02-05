package choral.amend.Quicksort;

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

					>> ArrayList@A< Integer >::new;
				pivot >> orderedList::add;
				greaterPartition
					>> qb::sort

					>> orderedList::addAll;
				return orderedList;
			} else {
				

				return a;
			}
		}

		private void partition(
				List@A< Integer > a, Integer@A pivot,
				List@B< Integer > greater, List@C< Integer > lower
		){
			if( a.size() > 0@A ){
				

				Integer@A i = a.remove( 0@A );
				if ( i > pivot ){
					

					i >>                          greater::add;
				} else {
					

					i >>                          lower::add;
				}
				partition( a, pivot, greater, lower );
			} else {
				
				
			}
		}
}
