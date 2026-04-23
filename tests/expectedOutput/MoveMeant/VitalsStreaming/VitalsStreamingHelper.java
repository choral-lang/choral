package MoveMeant.VitalsStreaming;

import MoveMeant.VitalsStreaming.utils.PatientsRegistry;
import MoveMeant.VitalsStreaming.utils.Signature;
import MoveMeant.VitalsStreaming.utils.SignatureRegistry;
import MoveMeant.VitalsStreaming.utils.Vitals;
import choral.annotations.Choreography;

@Choreography( role = "A", name = "VitalsStreamingHelper" )
class VitalsStreamingHelper {
	static Vitals pseudonymise( Vitals vitals ) {
		return new Vitals( PatientsRegistry.getPseudoID( vitals.id() ), vitals.heartRate(), vitals.temperature(), vitals.motion() );
	}
	
	static Boolean checkSignature( Signature signature ) {
		return SignatureRegistry.isValid( signature );
	}

}
