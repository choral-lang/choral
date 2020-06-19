package choral.examples.Mergesort;

import org.choral.channels.SymChannel;
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
			ch_AB.< MChoice >select( MChoice@A.L ); ch_CA.< MChoice >select( MChoice@A.L );
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
			ch_AB.< MChoice >select( MChoice@A.R );	ch_CA.< MChoice >select( MChoice@A.R );
			return a;
		}
	}

	private List@A< Integer > merge ( List@B< Integer> lhs, List@C< Integer> rhs ) {
		if( lhs.size() > 0@B ) {
			ch_AB.< MChoice >select( MChoice@B.L );	ch_BC.< MChoice >select( MChoice@B.L );
			if( rhs.size() > 0@C ){
				ch_CA.< MChoice >select( MChoice@C.L );	ch_BC.< MChoice >select( MChoice@C.L );
				ArrayList@A< Integer > result = new ArrayList@A< Integer >();
				if( lhs.get( 0@B ) <= ch_BC.< Integer >com( rhs.get( 0@C ) ) ){
					ch_AB.< MChoice >select( MChoice@B.L );	ch_BC.< MChoice >select( MChoice@B.L );
					lhs.get( 0@B ) >> ch_AB::< Integer >com >> result::add;
					merge( lhs.subList( 1@B, lhs.size() ), rhs ) >> result::addAll;
					return result;
				} else {
					ch_AB.< MChoice >select( MChoice@B.R );	ch_BC.< MChoice >select( MChoice@B.R );
					rhs.get( 0@C ) >> ch_CA::< Integer >com >> result::add;
					merge( lhs, rhs.subList( 1@C, rhs.size() ) ) >> result::addAll;
					return result;
				}
			} else {
				ch_CA.< MChoice >select( MChoice@C.R );	ch_BC.< MChoice >select( MChoice@C.R );
				return lhs >> ch_AB::< List< Integer > >com;
			}
		} else {
			ch_AB.< MChoice >select( MChoice@B.R );	ch_BC.< MChoice >select( MChoice@B.R );
			return rhs >> ch_CA::< List< Integer > >com;
		}
	}

}
