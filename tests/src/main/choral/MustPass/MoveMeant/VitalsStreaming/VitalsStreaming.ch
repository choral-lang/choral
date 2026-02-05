package choral.MustPass.MoveMeant.VitalsStreaming;

import choral.channels.SymChannel;
import java.util.function.Consumer;
import choral.MustPass.MoveMeant.VitalsStreaming.utils.PatientsRegistry;
import choral.MustPass.MoveMeant.VitalsStreaming.utils.Sensor;
import choral.MustPass.MoveMeant.VitalsStreaming.utils.Signature;
import choral.MustPass.MoveMeant.VitalsStreaming.utils.SignatureRegistry;
import choral.MustPass.MoveMeant.VitalsStreaming.utils.Vitals;
import choral.MustPass.MoveMeant.VitalsStreaming.utils.VitalsMsg;

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
