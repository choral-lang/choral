package choral.examples.VitalsStreaming;

import choral.examples.VitalsStreamingUtils.SignatureRegistry;
import choral.examples.VitalsStreamingUtils.Signature;
import choral.examples.VitalsStreamingUtils.Vitals;
import choral.annotations.Choreography;
import choral.examples.VitalsStreamingUtils.PatientsRegistry;

@Choreography( role = "A", name = "VitalsStreamingHelper" )
class VitalsStreamingHelper {
	static Vitals pseudonymise( Vitals vitals ) {
		return new Vitals( PatientsRegistry.getPseudoID( vitals.id() ), vitals.heartRate(), vitals.temperature(), vitals.motion() );
	}
	
	static Boolean checkSignature( Signature signature ) {
		return SignatureRegistry.isValid( signature );
	}

}
