package choral.examples.VitalsStreaming;
import org.choral.annotations.Choreography;

@Choreography( role = "E", name = "CheckSignature" )
enum CheckSignature {
	VALID, INVALID
}
