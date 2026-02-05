package choral.MustPass.MoveMeant.Mergesort;

import choral.channels.SymChannel;
import java.util.ArrayList;
import java.util.List;

enum MChoice@R { L, R }

public class Mergesort@( A, B, C ){

	SymChannel@( A, B )< Object > ch_AB;
	SymChannel@( B, C )< Object > ch_BC;
	SymChannel@( C, A )< Object > ch_CA;

	public Mergesort(
		SymChannel@( A, B )< Object > ch_AB,
		SymChannel@( B, C )< Object > ch_BC,
		SymChannel@( C, A )< Object > ch_CA
	){
		this.ch_AB = ch_AB; this.ch_BC = ch_BC;	this.ch_CA = ch_CA;
	}

	public List@A< Integer > sort ( List@A< Integer > a ){
		if( a.size() > 1@A ){
			
			Mergesort@( B, C, A ) mb = new Mergesort@( B, C, A )( ch_BC, ch_CA, ch_AB );
			Mergesort@( C, A, B ) mc = new Mergesort@( C, A, B )( ch_CA, ch_AB, ch_BC );
			Double@A pivot = a.size() / 2@A
				>> Math@A::floor
				>> Double@A::valueOf;
			List@B< Integer > lhs = a.subList( 0@A, pivot.intValue() )
				
				>> mb::sort;
			List@C< Integer > rhs = a.subList( pivot.intValue(), a.size() )
				
				>> mc::sort;
			return merge( lhs, rhs );
		} else {
			
			return a;
		}
	}

	private List@A< Integer > merge ( List@B< Integer> lhs, List@C< Integer> rhs ) {
		if( lhs.size() > 0@B ) {
			
			if( rhs.size() > 0@C ){
				
				ArrayList@A< Integer > result = new ArrayList@A< Integer >();
				if( lhs.get( 0@B ) <= rhs.get( 0@C ) ){
					
					lhs.get( 0@B )                          >> result::add;
					merge( lhs.subList( 1@B, lhs.size() ), rhs ) >> result::addAll;
					return result;
				} else {
					
					rhs.get( 0@C )                          >> result::add;
					merge( lhs, rhs.subList( 1@C, rhs.size() ) ) >> result::addAll;
					return result;
				}
			} else {
				
				return lhs;
			}
		} else {
			
			return rhs;
		}
	}

}
