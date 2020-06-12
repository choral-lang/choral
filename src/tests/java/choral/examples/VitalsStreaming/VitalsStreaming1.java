package choral.examples.VitalsStreaming;
import choral.examples.VitalsStreamingUtils.*;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "Device", name = "VitalsStreaming" )
public class VitalsStreaming1 {
	private SymChannel1 < Object > ch;
	private Sensor sensor;

	public VitalsStreaming1( SymChannel1 < Object > ch, Sensor sensor ) {
		this.ch = ch;
		this.sensor = sensor;
	}

	private Unit pseudonymise( Unit vitals ) {
		return Unit.id;
	}

	private Unit checkSignature( Unit signature ) {
		return Unit.id;
	}

	public void gather( Unit consumer ) {
		gather();
	}

	public void gather() {
		if( sensor.isOn() ){
			ch.< StreamState >select( StreamState.ON );
			ch.< VitalsMsg >com( sensor.next() );
			this.checkSignature( Unit.id );
			gather( Unit.id );
		} else {
			ch.< StreamState >select( StreamState.OFF );
		}
	}

}
