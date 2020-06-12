package choral.examples.VitalsStreaming;
import choral.examples.VitalsStreamingUtils.*;
import org.choral.lang.Channels.SymChannel2;

import java.util.function.Consumer;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "Gatherer", name = "VitalsStreaming" )
public class VitalsStreaming2 {
	private SymChannel2 < Object > ch;

	public VitalsStreaming2( SymChannel2 < Object > ch, Unit sensor ) {
		this( ch );
	}

	public VitalsStreaming2( SymChannel2 < Object > ch ) {
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
						consumer.accept( this.pseudonymise( msg.content() ) );
					}
					gather( consumer );
				}
			}
		}
	}

}
