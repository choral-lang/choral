package choral.MustPass.MoveMeant.SplitAndCombine;

import choral.MustPass.MoveMeant.SplitAndCombine.utils.*;
import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "Worker2", name = "SplitAndCombine" )
public class SplitAndCombine_Worker2 {
	public static Unit splitAndCombine( Unit ch_MW1, SymChannel_B < Object > ch_MW2, Unit task ) {
		return splitAndCombine( ch_MW2 );
	}
	
	public static Unit splitAndCombine( SymChannel_B < Object > ch_MW2 ) {
		Task msg1 = ch_MW2.< Task >com( Unit.id );
		Task sub2 = msg1;
		Result res2 = Worker.run( sub2 );
		ch_MW2.< Result >com( res2 );
		return Unit.id;
	}

}
