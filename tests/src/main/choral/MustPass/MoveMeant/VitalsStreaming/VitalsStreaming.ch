package choral.amend.vitalsstreaming;

import choral.channels.SymChannel;
import java.util.function.Consumer;
import choral.amend.vitalsstreaming.utils.PatientsRegistry;
import choral.amend.vitalsstreaming.utils.Sensor;
import choral.amend.vitalsstreaming.utils.Signature;
import choral.amend.vitalsstreaming.utils.SignatureRegistry;
import choral.amend.vitalsstreaming.utils.Vitals;
import choral.amend.vitalsstreaming.utils.VitalsMsg;

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
			
			VitalsMsg@Gatherer msg = sensor.next()                        ;
			Boolean@Gatherer checkSignature = msg.signature() >> VitalsStreamingHelper@Gatherer::checkSignature;
			if ( checkSignature ) {
				msg.content() >> VitalsStreamingHelper@Gatherer::pseudonymise >> consumer::accept;
			}
			gather( consumer );
		} else {
			
		}
	}
}
