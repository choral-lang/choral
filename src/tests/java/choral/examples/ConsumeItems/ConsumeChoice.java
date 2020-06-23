package choral.examples.ConsumeItems;
import org.choral.annotations.Choreography;

@Choreography( role = "R", name = "ConsumeChoice" )
public enum ConsumeChoice {
	AGAIN, STOP
}
