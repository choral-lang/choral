package choral.examples.Mergesort;
import org.choral.channels.SymChannel_A;
import org.choral.lang.Unit;
import org.choral.channels.SymChannel_B;
import java.util.List;
import org.choral.annotations.Choreography;

@Choreography( role = "C", name = "Mergesort" )
public class Mergesort_C {
	SymChannel_B < Object > ch_BC;
	SymChannel_A < Object > ch_CA;

	public Mergesort_C( Unit ch_AB, SymChannel_B < Object > ch_BC, SymChannel_A < Object > ch_CA ) {
		this( ch_BC, ch_CA );
	}
	
	public Mergesort_C( SymChannel_B < Object > ch_BC, SymChannel_A < Object > ch_CA ) {
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
					Mergesort_B mb;
					mb = new Mergesort_B( ch_BC, ch_CA, Unit.id );
					Mergesort_A mc;
					mc = new Mergesort_A( ch_CA, Unit.id, ch_BC );
					mb.sort( Unit.id );
					List < Integer > rhs;
					rhs = mc.sort( ch_CA.< List < Integer > >com( Unit.id ) );
					return merge( Unit.id, rhs );
				}
			}
		}
	}

}
