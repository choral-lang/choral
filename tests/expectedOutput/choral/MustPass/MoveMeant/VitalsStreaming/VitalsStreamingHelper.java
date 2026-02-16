package choral.MustPass.MoveMeant.VitalsStreaming;

import choral.MustPass.MoveMeant.VitalsStreaming.utils.PatientsRegistry;
import choral.MustPass.MoveMeant.VitalsStreaming.utils.Signature;
import choral.MustPass.MoveMeant.VitalsStreaming.utils.SignatureRegistry;
import choral.MustPass.MoveMeant.VitalsStreaming.utils.Vitals;
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
