package choral.MustPass.MoveMeant.SplitAndCombine;

import choral.channels.SymChannel;

import choral.MustPass.MoveMeant.SplitAndCombine.utils.*;

public class SplitAndCombine@( Main, Worker1, Worker2 ){
    public static Result@Main splitAndCombine( 
        SymChannel@( Main, Worker1 )<Object> ch_MW1, 
        SymChannel@( Main, Worker2 )<Object> ch_MW2,
        Task@Main task 
    ){
        Tasks@Main tasks = Main@Main.split(task);
        Task@Worker1 sub1 =                     tasks.first();
        Task@Worker2 sub2 =                     tasks.second();

        Result@Worker1 res1 = Worker@Worker1.run( sub1 );
        Result@Worker2 res2 = Worker@Worker2.run( sub2 );

        Result@Main res1_M =                     res1;
        Result@Main res2_M =                     res2;
        
        return Main@Main.combine( res1_M, res2_M );
    }
}