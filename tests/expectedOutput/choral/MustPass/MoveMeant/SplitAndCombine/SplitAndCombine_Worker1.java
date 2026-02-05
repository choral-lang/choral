package choral.MustPass.MoveMeant.SplitAndCombine;

import choral.MustPass.MoveMeant.SplitAndCombine.utils.*;
import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "Worker1", name = "SplitAndCombine" )
public class SplitAndCombine_Worker1 {
	public static Unit splitAndCombine( SymChannel_B < Object > ch_MW1, Unit ch_MW2, Unit task ) {
		return splitAndCombine( ch_MW1 );
	}
	
	public static Unit splitAndCombine( SymChannel_B < Object > ch_MW1 ) {
		Task dependencyAtWorker1_1082862213 = ch_MW1.< Task >com( Unit.id );
		Task sub1 = dependencyAtWorker1_1082862213;
		Result res1 = Worker.run( sub1 );
		ch_MW1.< Result >com( res1 );
		return Unit.id;
	}

}
