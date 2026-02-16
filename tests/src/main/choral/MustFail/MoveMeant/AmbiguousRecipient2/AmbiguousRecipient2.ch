package choral.MustPass.MoveMeant.AmbiguousRecipient2;

import choral.channels.SymChannel;
import choral.MustPass.MoveMeant.AmbiguousRecipient2.utils.Client;


public class AmbiguousRecipient2@(A,B,C){
    
    SymChannel@( A, B )< Object > ch_AB;
    SymChannel@( A, C )< Object > ch_AC;
    SymChannel@( C, B )< Object > ch_CB;

    public void fun( Client@(A,B) client ){
        Integer@A I_A = 0@A;
        Integer@B I_B = 0@B;
        Integer@C I_C = 0@C;

        client.fun( I_A, I_A ); // no comms
        client.fun( I_A, I_B ); // no comms
        client.fun( I_B, I_A ); // I_B sent to A
        client.fun( I_C, I_B ); // I_C sent to A
        client.fun( I_C, I_C ); //! Cannot resolve method 'fun(java.lang.Integer@(C),java.lang.Integer@(C))'
    }
}

