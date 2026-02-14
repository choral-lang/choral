package choral.MustPass.MoveMeant.SimpleKOC;

import choral.channels.SymChannel;

class SimpleKOC@(A,B,C){
    public static void fun( SymChannel@(A,B)<Object> ch_AB, SymChannel@(A,C)<Object> ch_AC ){
        int@A IA = 0@A;
        int@B IB = 0@B;
        int@C IC = 0@C;

        if( IA < 1@A ){
            IA = IA + 1@A;
            IB = IB + 1@B;
            IC = IC + 1@C;
        } else{
            IA = IA - 1@A;
            IB = IB - 1@B;
            IC = IC - 1@C;
        }

        if( IA < 1@A ){
            IA = IA + 1@A;
            IB = IB + 1@B;
        }

    }
}