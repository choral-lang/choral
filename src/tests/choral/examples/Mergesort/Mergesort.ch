package choral.examples.Mergesort;

import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import java.util.ArrayList;
import java.util.List;

public enum MChoice@R { L, R }

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
			select( MChoice@A.L, ch_AB ); select( MChoice@A.L, ch_CA );
			Mergesort@( B, C, A ) mb = new Mergesort@( B, C, A )( ch_BC, ch_CA, ch_AB );
			Mergesort@( C, A, B ) mc = new Mergesort@( C, A, B )( ch_CA, ch_AB, ch_BC );
			Double@A pivot = a.size() / 2@A
				>> Math@A::floor
				>> Double@A::valueOf;
			List@B< Integer > lhs = a.subList( 0@A, pivot.intValue() )
				>> ch_AB::< List< Integer > >com
				>> mb::sort;
			List@C< Integer > rhs = a.subList( pivot.intValue(), a.size() )
				>> ch_CA::< List< Integer > >com
				>> mc::sort;
			return merge( lhs, rhs );
		} else {
			select( MChoice@A.R, ch_AB ); select( MChoice@A.R, ch_CA );
			return a;
		}
	}

	private List@A< Integer > merge ( List@B< Integer> lhs, List@C< Integer> rhs ) {
		if( lhs.size() > 0@B ) {
			select( MChoice@B.L, ch_AB ); select( MChoice@B.L, ch_BC );
			if( rhs.size() > 0@C ){
				select( MChoice@C.L, ch_CA ); select( MChoice@C.L, ch_BC );
				ArrayList@A< Integer > result = new ArrayList@A< Integer >();
				if( lhs.get( 0@B ) <= ch_BC.< Integer >com( rhs.get( 0@C ) ) ){
					select( MChoice@B.L, ch_AB ); select( MChoice@B.L, ch_BC );
					lhs.get( 0@B ) >> ch_AB::< Integer >com >> result::add;
					merge( lhs.subList( 1@B, lhs.size() ), rhs ) >> result::addAll;
					return result;
				} else {
					select( MChoice@B.R, ch_AB ); select( MChoice@B.R, ch_BC );
					rhs.get( 0@C ) >> ch_CA::< Integer >com >> result::add;
					merge( lhs, rhs.subList( 1@C, rhs.size() ) ) >> result::addAll;
					return result;
				}
			} else {
				select( MChoice@C.R, ch_CA ); select( MChoice@C.R, ch_BC );
				return lhs >> ch_AB::< List< Integer > >com;
			}
		} else {
			select( MChoice@B.R, ch_AB ); select( MChoice@B.R, ch_BC );
			return rhs >> ch_CA::< List< Integer > >com;
		}
	}

}
