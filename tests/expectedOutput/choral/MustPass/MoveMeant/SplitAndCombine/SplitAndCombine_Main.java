package choral.MustPass.MoveMeant.SplitAndCombine;

import choral.MustPass.MoveMeant.SplitAndCombine.utils.*;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "Main", name = "SplitAndCombine" )
public class SplitAndCombine_Main {
	public static Result splitAndCombine( SymChannel_A < Object > ch_MW1, SymChannel_A < Object > ch_MW2, Task task ) {
		Tasks tasks = Main.split( task );
		ch_MW1.< Task >com( tasks.first() );
		ch_MW2.< Task >com( tasks.second() );
		Result msg2 = ch_MW1.< Result >com( Unit.id );
		Result res1_M = msg2;
		Result msg3 = ch_MW2.< Result >com( Unit.id );
		Result res2_M = msg3;
		return Main.combine( res1_M, res2_M );
	}

}
