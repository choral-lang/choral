package choral.examples.ConsumeItems;
import java.util.Iterator;

import org.choral.lang.Channels.DiChannel1;
import org.choral.lang.Unit;

public class ConsumeItems_A {
	public static void consumeItems( DiChannel1 < Integer > ch, Iterator < Integer > it, Unit consumer ) {
		consumeItems( ch, it );
	}

	public static void consumeItems( DiChannel1< Integer > ch, Iterator < Integer > it ) {
		ch.< Integer >com( it.next() );
		if( it.hasNext() ){
			ch.< ConsumeChoice >select( ConsumeChoice.AGAIN );
			ch.< Integer >com( it.next() );
			consumeItems( ch, it, Unit.id );
		} else {
			ch.< ConsumeChoice >select( ConsumeChoice.STOP );
		}
	}

}
