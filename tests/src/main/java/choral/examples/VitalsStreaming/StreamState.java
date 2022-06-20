package choral.examples.VitalsStreaming;

import choral.annotations.Choreography;

@Choreography( role = "E", name = "StreamState" )
enum StreamState {
	ON,
	OFF
}
