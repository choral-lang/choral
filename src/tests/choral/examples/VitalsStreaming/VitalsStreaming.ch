package choral.examples.VitalsStreaming;

import org.choral.channels.SymChannel;
import java.util.function.Consumer;
import choral.examples.VitalsStreamingUtils.PatientsRegistry;
import choral.examples.VitalsStreamingUtils.Sensor;
import choral.examples.VitalsStreamingUtils.Signature;
import choral.examples.VitalsStreamingUtils.SignatureRegistry;
import choral.examples.VitalsStreamingUtils.Vitals;
import choral.examples.VitalsStreamingUtils.VitalsMsg;

enum StreamState@E { ON, OFF }
enum CheckSignature@E { VALID, INVALID }

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
			ch.< StreamState >select( StreamState@Device.ON );
			VitalsMsg@Gatherer msg = sensor.next() >> ch::< VitalsMsg >com;
			Boolean@Gatherer checkSignature = msg.signature() >> this::checkSignature;
			if ( checkSignature ) {
				ch.< CheckSignature >select( CheckSignature@Gatherer.VALID );
				msg.content() >> this::pseudonymise >> consumer::accept;
			} else { ch.< CheckSignature >select( CheckSignature@Gatherer.INVALID ); }
			gather( consumer );
		} else {
			ch.< StreamState >select( StreamState@Device.OFF );
		}
	}
}
