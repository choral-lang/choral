package choral.examples.VitalsStreaming;

import choral.lang.Unit;
import choral.examples.VitalsStreamingUtils.VitalsMsg;
import choral.channels.SymChannel_A;
import choral.examples.VitalsStreamingUtils.Sensor;

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
