package choral.examples.VitalsStreaming;

import choral.examples.VitalsStreamingUtils.*;
import org.choral.lang.Channels.SymChannel2;
import org.choral.lang.Channels.SymChannel1;
import java.util.function.Consumer;

public enum StreamState@E { ON, OFF }

public class VitalsStreaming@( Device, Gatherer ) {
	private SymChannel@( Device, Gatherer )< Object > ch;
	private Sensor@Device sensor;

	public VitalsStreaming(
		SymChannel@( Device, Gatherer )< Object > ch,
		Sensor@Device sensor
	) {
		this.ch = ch;
		this.sensor = sensor;
	}

	private Vitals@Gatherer	pseudonymise( Vitals@Gatherer vitals ) {
		return new Vitals@Gatherer(
			PatientsRegistry@Gatherer.getPseudoID( vitals.id() ),
			vitals.heartRate(),
			vitals.temperature(),
			vitals.motion()
		);
	}

	private Boolean@Gatherer checkSignature( Signature@Gatherer signature ) {
		return SignatureRegistry@Gatherer.isValid( signature );
	}

	public void gather( Consumer@Gatherer< Vitals > consumer ) {
		if( sensor.isOn() ){
			select( StreamState@Device.ON, ch );
			VitalsMsg@Gatherer msg = sensor.next() >> ch::< VitalsMsg >com;
			Boolean@Gatherer checkSignature = msg.signature() >> this::checkSignature;
			if ( checkSignature ) {
				msg.content() >> this::pseudonymise >> consumer::accept;
			}
			gather( consumer );
		} else {
			select( StreamState@Device.OFF, ch );
		}
	}
}
