package choral.examples.VitalsStreaming;

import choral.channels.SymChannel;
import java.util.function.Consumer;
import choral.examples.VitalsStreamingUtils.PatientsRegistry;
import choral.examples.VitalsStreamingUtils.Sensor;
import choral.examples.VitalsStreamingUtils.Signature;
import choral.examples.VitalsStreamingUtils.SignatureRegistry;
import choral.examples.VitalsStreamingUtils.Vitals;
import choral.examples.VitalsStreamingUtils.VitalsMsg;

enum StreamState@E { ON, OFF }

class VitalsStreamingHelper@A {

	static Vitals@A	pseudonymise( Vitals@A vitals ) {
		return new Vitals@A(
			PatientsRegistry@A.getPseudoID( vitals.id() ),
			vitals.heartRate(),
			vitals.temperature(),
			vitals.motion()
		);
	}

	static Boolean@A checkSignature( Signature@A signature ) {
		return SignatureRegistry@A.isValid( signature );
	}

}

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

	public void gather( Consumer@Gatherer< Vitals > consumer ) {
		if( sensor.isOn() ){
			ch.< StreamState >select( StreamState@Device.ON );
			VitalsMsg@Gatherer msg = sensor.next() >> ch::< VitalsMsg >com;
			Boolean@Gatherer checkSignature = msg.signature() >> VitalsStreamingHelper@Gatherer::checkSignature;
			if ( checkSignature ) {
				msg.content() >> VitalsStreamingHelper@Gatherer::pseudonymise >> consumer::accept;
			}
			gather( consumer );
		} else {
			ch.< StreamState >select( StreamState@Device.OFF );
		}
	}
}
