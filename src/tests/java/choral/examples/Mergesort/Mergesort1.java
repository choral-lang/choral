package choral.examples.Mergesort;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import java.util.ArrayList;
import java.util.List;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "A", name = "Mergesort" )
public class Mergesort1 {
	SymChannel1 < Object > ch_AB;
	SymChannel2 < Object > ch_CA;

	public Mergesort1( SymChannel1 < Object > ch_AB, Unit ch_BC, SymChannel2 < Object > ch_CA ) {
		this( ch_AB, ch_CA );
	}

	public Mergesort1( SymChannel1 < Object > ch_AB, SymChannel2 < Object > ch_CA ) {
		this.ch_AB = ch_AB;
		this.ch_CA = ch_CA;
	}

	public List < Integer > sort( List < Integer > a ) {
		if( a.size() > 1 ){
			ch_AB.< MChoice >select( MChoice.L );
			ch_CA.< MChoice >select( MChoice.L );
			Mergesort3 mb;
			mb = new Mergesort3( Unit.id, ch_CA, ch_AB );
			Mergesort2 mc;
			mc = new Mergesort2( ch_CA, ch_AB, Unit.id );
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
								result = new ArrayList< Integer >();
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
