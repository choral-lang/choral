package choral.MustPass.MoveMeant.VitalsStreaming;

import choral.MustPass.MoveMeant.VitalsStreaming.utils.Sensor;
import choral.MustPass.MoveMeant.VitalsStreaming.utils.VitalsMsg;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

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
			ch.< KOCEnum >select( KOCEnum.CASE0 );
			ch.< VitalsMsg >com( sensor.next() );
			gather( Unit.id );
		} else { 
			ch.< KOCEnum >select( KOCEnum.CASE1 );
		}
	}

}
