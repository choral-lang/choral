package choral.examples.VitalsStreaming;
import org.choral.lang.Unit;
import choral.examples.VitalsStreamingUtils.VitalsMsg;
import choral.examples.VitalsStreamingUtils.Signature;
import choral.examples.VitalsStreamingUtils.PatientsRegistry;
import choral.examples.VitalsStreamingUtils.SignatureRegistry;
import java.util.function.Consumer;
import org.choral.channels.SymChannel_B;
import choral.examples.VitalsStreamingUtils.Vitals;
import org.choral.annotations.Choreography;

@Choreography( role = "Gatherer", name = "VitalsStreaming" )
public class VitalsStreaming_Gatherer {
	private SymChannel_B < Object > ch;

	public VitalsStreaming_Gatherer( SymChannel_B < Object > ch, Unit sensor ) {
		this( ch );
	}
	
	public VitalsStreaming_Gatherer( SymChannel_B < Object > ch ) {
		this.ch = ch;
	}

	private Vitals pseudonymise( Vitals vitals ) {
		return new Vitals( PatientsRegistry.getPseudoID( vitals.id() ), vitals.heartRate(), vitals.temperature(), vitals.motion() );
	}
	
	private Boolean checkSignature( Signature signature ) {
		return SignatureRegistry.isValid( signature );
	}
	
	public void gather( Consumer < Vitals > consumer ) {
		{
			switch( ch.< StreamState >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case OFF -> {
					
				}
				case ON -> {
					VitalsMsg msg;
					msg = ch.< VitalsMsg >com( Unit.id );
					Boolean checkSignature;
					checkSignature = this.checkSignature( msg.signature() );
					if( checkSignature ){
						ch.< CheckSignature >select( CheckSignature.VALID );
						consumer.accept( this.pseudonymise( msg.content() ) );
					} else { 
						ch.< CheckSignature >select( CheckSignature.INVALID );
					}
					gather( consumer );
				}
			}
		}
	}

}
