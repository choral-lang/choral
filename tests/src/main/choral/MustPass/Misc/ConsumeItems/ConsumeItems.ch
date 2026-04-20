package choral.MustPass.ConsumeItems;

import choral.channels.DiChannel;
import java.util.function.Consumer;
import java.util.Iterator;

public class ConsumeItems@( A, B ) {
	public static void consumeItems( DiChannel@( A, B )< Integer > ch, Iterator@A< Integer > it, Consumer@B< Integer > consumer ){
		ch.< Integer >com( it.next() );
		if ( it.hasNext() ){
			ch.< ConsumeChoice >select( ConsumeChoice@A.AGAIN );
			it.next() >> ch::< Integer > com >> consumer::accept;
			consumeItems( ch, it, consumer );
		} else {
			ch.< ConsumeChoice >select( ConsumeChoice@A.STOP );
		}
    }
}
