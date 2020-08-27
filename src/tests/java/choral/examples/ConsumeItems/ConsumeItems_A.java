package choral.examples.ConsumeItems;
import org.choral.annotations.Choreography;
import org.choral.channels.DiChannel_A;
import java.util.Iterator;
import org.choral.lang.Unit;

@Choreography( role = "A", name = "ConsumeItems" )
public class ConsumeItems_A {
	public static void consumeItems( DiChannel_A < Integer > ch, Iterator < Integer > it, Unit consumer ) {
		consumeItems( ch, it );
	}
	
	public static void consumeItems( DiChannel_A < Integer > ch, Iterator < Integer > it ) {
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