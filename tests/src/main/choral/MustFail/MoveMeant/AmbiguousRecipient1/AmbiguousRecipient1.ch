package choral.MustPass.MoveMeant.AmbiguousRecipient1;

import choral.channels.SymChannel;
import choral.MustPass.MoveMeant.AmbiguousRecipient1.utils.Client;


public class AmbiguousRecipient1@(A,B,C){
    
    SymChannel@( A, B )< Object > ch_AB;
    SymChannel@( A, C )< Object > ch_AC;
    SymChannel@( C, B )< Object > ch_CB;

    public void fun( Client@(A,B) client ){
        Integer@A I_A = 0@A;
        Integer@B I_B = 0@B;
        Integer@C I_C = 0@C;

        client.fun( I_A );
        client.fun( I_B );
        client.fun( I_C ); //! Ambiguous method invocation, fun(java.lang.Integer@(A)) and fun(java.lang.Integer@(B))
    }
}

