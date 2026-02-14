package choral.MustPass.MoveMeant.OverloadOnRoles;

import choral.channels.SymChannel;
import java.lang.Object;
import java.lang.Integer;

class Client@(A,B) {

	Object@A obj;

	public void fun( Integer@A in ){}

    public void fun( Integer@B in ){}

    public void fun2( Integer@A in1, Integer@A in2 ){}

    public void fun2( Integer@A in1, Integer@B in2 ){}

}

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

        client.fun2( I_A, I_A ); // no comms
        client.fun2( I_A, I_B ); // no comms
        client.fun2( I_B, I_A ); // I_B sent to A
        client.fun2( I_C, I_B ); // I_C sent to A
    }
}

