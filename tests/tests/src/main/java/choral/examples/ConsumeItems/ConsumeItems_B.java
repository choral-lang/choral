package choral.examples.ConsumeItems;

import choral.lang.Unit;
import choral.annotations.Choreography;
import choral.channels.DiChannel_B;
import java.util.function.Consumer;

@Choreography( role = "B", name = "ConsumeItems" )
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
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}

}
