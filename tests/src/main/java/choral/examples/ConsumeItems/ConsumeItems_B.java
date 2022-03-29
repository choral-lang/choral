package choral.examples.ConsumeItems;

import java.util.function.Consumer;
import choral.lang.Unit;
import choral.channels.DiChannel_B;

public class ConsumeItems_B {
	public static void consumeItems( DiChannel_B < Integer > ch, Unit it, Consumer < Integer > consumer ) {
		consumeItems( ch, consumer );
	}
	
	public static void consumeItems( DiChannel_B < Integer > ch, Consumer < Integer > consumer ) {
		ch.< Integer >com( Unit.id );
		{
			switch( ch.< ConsumeChoice >select( Unit.id ) ){
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
