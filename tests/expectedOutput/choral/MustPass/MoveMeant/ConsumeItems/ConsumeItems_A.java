package choral.MustPass.MoveMeant.ConsumeItems;

import choral.annotations.Choreography;
import choral.channels.DiChannel_A;
import choral.lang.Unit;
import java.util.Iterator;

@Choreography( role = "A", name = "ConsumeItems" )
public class ConsumeItems_A {
	public static void consumeItems( DiChannel_A < Object > ch, Iterator < Integer > it, Unit consumer ) {
		consumeItems( ch, it );
	}
	
	public static void consumeItems( DiChannel_A < Object > ch, Iterator < Integer > it ) {
		if( it.hasNext() ){
			ch.< KOCEnum >select( KOCEnum.CASE0 );
			ch.< Integer >com( it.next() );
			consumeItems( ch, it, Unit.id );
		} else { 
			ch.< KOCEnum >select( KOCEnum.CASE1 );
		}
	}

}
