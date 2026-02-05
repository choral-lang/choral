package choral.MustPass.MoveMeant.VitalsStreaming;

import choral.MustPass.MoveMeant.VitalsStreaming.utils.Vitals;
import choral.MustPass.MoveMeant.VitalsStreaming.utils.VitalsMsg;
import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;
import java.util.function.Consumer;

@Choreography( role = "Gatherer", name = "VitalsStreaming" )
public class VitalsStreaming_Gatherer {
	private SymChannel_B < Object > ch;

	public VitalsStreaming_Gatherer( SymChannel_B < Object > ch, Unit sensor ) {
		this( ch );
	}
	
	public VitalsStreaming_Gatherer( SymChannel_B < Object > ch ) {
		this.ch = ch;
	}

	public void gather( Consumer < Vitals > consumer ) {
		switch( ch.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				VitalsMsg dependencyAtGatherer_1591562332 = ch.< VitalsMsg >com( Unit.id );
				VitalsMsg msg = dependencyAtGatherer_1591562332;
				Boolean checkSignature = VitalsStreamingHelper.checkSignature( msg.signature() );
				if( checkSignature ){
					consumer.accept( VitalsStreamingHelper.pseudonymise( msg.content() ) );
				}
				gather( consumer );
			}
			case CASE1 -> {
				
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
