package choral.examples.ConsumeItems;
import org.choral.annotations.Choreography;
import java.util.function.Consumer;
import org.choral.channels.DiChannel_B;
import org.choral.lang.Unit;

@Choreography( role = "B", name = "ConsumeItems" )
public class ConsumeItems_B {
	public static void consumeItems( DiChannel_B < Integer > ch, Unit it, Consumer < Integer > consumer ) {
		consumeItems( ch, consumer );
	}
	
	public static void consumeItems( DiChannel_B < Integer > ch, Consumer < Integer > consumer ) {
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
