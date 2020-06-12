package choral.examples.Mergesort;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;

import java.util.List;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "C", name = "Mergesort" )
public class Mergesort3 {
	SymChannel2 < Object > ch_BC;
	SymChannel1 < Object > ch_CA;

	public Mergesort3( Unit ch_AB, SymChannel2 < Object > ch_BC, SymChannel1 < Object > ch_CA ) {
		this( ch_BC, ch_CA );
	}

	public Mergesort3( SymChannel2 < Object > ch_BC, SymChannel1 < Object > ch_CA ) {
		this.ch_BC = ch_BC;
		this.ch_CA = ch_CA;
	}

	public Unit sort( Unit a ) {
		return sort();
	}

	private Unit merge( Unit lhs, List < Integer > rhs ) {
		{
			switch( ch_BC.< MChoice >select( Unit.id ) ){
				case R -> {
					return ch_CA.< List < Integer > >com( rhs );
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case L -> {
					if( rhs.size() > 0 ){
						ch_CA.< MChoice >select( MChoice.L );
						ch_BC.< MChoice >select( MChoice.L );
						{
							ch_BC.< Integer >com( rhs.get( 0 ) );
							switch( ch_BC.< MChoice >select( Unit.id ) ){
								case R -> {
									ch_CA.< Integer >com( rhs.get( 0 ) );
									merge( Unit.id, rhs.subList( 1, rhs.size() ) );
									return Unit.id;
								}
								default -> {
									throw new RuntimeException( "Received unexpected label from select operation" );
								}
								case L -> {
									merge( Unit.id, rhs );
									return Unit.id;
								}
							}
						}
					} else {
						ch_CA.< MChoice >select( MChoice.R );
						ch_BC.< MChoice >select( MChoice.R );
						return Unit.id;
					}
				}
			}
		}
	}

	public Unit sort() {
		{
			switch( ch_CA.< MChoice >select( Unit.id ) ){
				case R -> {
					return Unit.id;
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case L -> {
					Mergesort2 mb;
					mb = new Mergesort2( ch_BC, ch_CA, Unit.id );
					Mergesort1 mc;
					mc = new Mergesort1( ch_CA, Unit.id, ch_BC );
					mb.sort( Unit.id );
					List < Integer > rhs;
					rhs = mc.sort( ch_CA.< List < Integer > >com( Unit.id ) );
					return merge( Unit.id, rhs );
				}
			}
		}
	}

}
