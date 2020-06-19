package choral.examples.VitalsStreaming;
import org.choral.lang.Unit;
import org.choral.channels.SymChannel_A;
import choral.examples.VitalsStreamingUtils.Sensor;
import choral.examples.VitalsStreamingUtils.VitalsMsg;
import org.choral.annotations.Choreography;

@Choreography( role = "Device", name = "VitalsStreaming" )
public class VitalsStreaming_Device {
	private SymChannel_A < Object > ch;
	private Sensor sensor;

	public VitalsStreaming_Device( SymChannel_A < Object > ch, Sensor sensor ) {
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
			{
				switch( ch.< CheckSignature >select( Unit.id ) ){
					default -> {
						throw new RuntimeException( "Received unexpected label from select operation" );
					}
					case VALID -> {
						this.pseudonymise( Unit.id );
					}
					case INVALID -> {
						
					}
				}
			}
			gather( Unit.id );
		} else { 
			ch.< StreamState >select( StreamState.OFF );
		}
	}

}
