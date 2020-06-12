package choral.examples.ConsumeItems;
import java.util.function.Consumer;
import java.util.Iterator;

import org.choral.lang.Channels.DiChannel2;
import org.choral.lang.Unit;

public class ConsumeItems_B {
	public static void consumeItems( DiChannel2< Integer > ch, Unit it, Consumer < Integer > consumer ) {
		consumeItems( ch, consumer );
	}

	public static void consumeItems( DiChannel2 < Integer > ch, Consumer < Integer > consumer ) {
		ch.< Integer >com( Unit.id );
		{
			switch( ch.< ConsumeChoice >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case AGAIN -> {
					consumer.accept( ch.< Integer >com( Unit.id ) );
					consumeItems( ch, Unit.id, consumer );
				}
				case STOP -> {

				}
			}
		}
	}

}
