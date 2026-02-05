package choral.MustPass.MoveMeant.ConsumeItems;

import choral.annotations.Choreography;
import choral.channels.DiChannel_B;
import choral.lang.Unit;
import java.util.function.Consumer;

@Choreography( role = "B", name = "ConsumeItems" )
public class ConsumeItems_B {
	public static void consumeItems( DiChannel_B < Object > ch, Unit it, Consumer < Integer > consumer ) {
		consumeItems( ch, consumer );
	}
	
	public static void consumeItems( DiChannel_B < Object > ch, Consumer < Integer > consumer ) {
		switch( ch.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				Integer msg0 = ch.< Integer >com( Unit.id );
				consumer.accept( msg0 );
				consumeItems( ch, Unit.id, consumer );
			}
			case CASE1 -> {
				
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
