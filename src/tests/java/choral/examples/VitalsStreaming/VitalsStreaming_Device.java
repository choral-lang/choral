package choral.examples.VitalsStreaming;
import org.choral.annotations.Choreography;
import choral.examples.VitalsStreamingUtils.VitalsMsg;
import choral.examples.VitalsStreamingUtils.Sensor;
import org.choral.channels.SymChannel_A;
import org.choral.lang.Unit;

@Choreography( role = "Device", name = "VitalsStreaming" )
public class VitalsStreaming_Device {
	private SymChannel_A < Object > ch;
	private Sensor sensor;

	public VitalsStreaming_Device( SymChannel_A < Object > ch, Sensor sensor ) {
		this.ch = ch;
		this.sensor = sensor;
	}

	public void gather( Unit consumer ) {
		gather();
	}
	
	public void gather() {
		if( sensor.isOn() ){
			ch.< StreamState >select( StreamState.ON );
			ch.< VitalsMsg >com( sensor.next() );
			gather( Unit.id );
		} else { 
			ch.< StreamState >select( StreamState.OFF );
		}
	}

}
