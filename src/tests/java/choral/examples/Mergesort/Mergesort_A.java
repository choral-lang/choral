package choral.examples.Mergesort;
import choral.lang.Unit;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
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
			ch_AB.< MChoice >select( MChoice.L );
			ch_CA.< MChoice >select( MChoice.L );
			Mergesort_C mb;
			mb = new Mergesort_C( Unit.id, ch_CA, ch_AB );
			Mergesort_B mc;
			mc = new Mergesort_B( ch_CA, ch_AB, Unit.id );
			Double pivot;
			pivot = Double.valueOf( Math.floor( a.size() / 2 ) );
			mb.sort( ch_AB.< List < Integer > >com( a.subList( 0, pivot.intValue() ) ) );
			mc.sort( ch_CA.< List < Integer > >com( a.subList( pivot.intValue(), a.size() ) ) );
			return merge( Unit.id, Unit.id );
		} else { 
			ch_AB.< MChoice >select( MChoice.R );
			ch_CA.< MChoice >select( MChoice.R );
			return a;
		}
	}
	
	private List < Integer > merge( Unit lhs, Unit rhs ) {
		{
			switch( ch_AB.< MChoice >select( Unit.id ) ){
				case R -> {
					return ch_CA.< List < Integer > >com( Unit.id );
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case L -> {
					{
						switch( ch_CA.< MChoice >select( Unit.id ) ){
							case R -> {
								return ch_AB.< List < Integer > >com( Unit.id );
							}
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
							}
							case L -> {
								ArrayList < Integer > result;
								result = new ArrayList < Integer >();
								{
									switch( ch_AB.< MChoice >select( Unit.id ) ){
										case R -> {
											result.add( ch_CA.< Integer >com( Unit.id ) );
											result.addAll( merge( Unit.id, Unit.id ) );
											return result;
										}
										default -> {
											throw new RuntimeException( "Received unexpected label from select operation" );
										}
										case L -> {
											result.add( ch_AB.< Integer >com( Unit.id ) );
											result.addAll( merge( Unit.id, Unit.id ) );
											return result;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

}
