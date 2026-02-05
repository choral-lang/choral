package choral.amend.overloadonroles;

import choral.channels.SymChannel;
import choral.amend.overloadonroles.utils.Client;


public class OverloadOnRoles@(A,B,C){
    
    SymChannel@( A, B )< Object > ch_AB;
    SymChannel@( A, C )< Object > ch_AC;
    SymChannel@( C, B )< Object > ch_CB;

    public void fun( Client@(A,B) client ){
        Integer@A I_A = 0@A;
        Integer@B I_B = 0@B;
        Integer@C I_C = 0@C;

        client.fun( I_A );
        client.fun( I_B );
        // client.fun( I_C ); // illegal

        client.fun( I_A, I_A ); // no comms
        client.fun( I_A, I_B ); // no comms
        client.fun( I_B, I_A ); // I_B sent to A
        client.fun( I_C, I_B ); // I_C sent to A
        // client.fun( I_C, I_C ); // illegal
    }
}

