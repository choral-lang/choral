package choral.MustPass.MoveMeant.SSOWithRetry;

import choral.channels.SymChannel;
import choral.MustPass.MoveMeant.SSOWithRetry.utils.*;

import choral.runtime.Serializers.KryoSerializable;

enum Validity@R{ TOKEN, INVALID }
enum Retry@R{ RETRY, ERROR }

public class SSOWithRetry@( C, S, CAS ){
    SymChannel@( C, S )<Object> ch_CS;
    SymChannel@( S, CAS )<Object> ch_SCAS;
    SymChannel@( CAS, C )<Object> ch_CASC;

    public SSOWithRetry(
        SymChannel@( C, S )<Object> ch_CS,
        SymChannel@( S, CAS )<Object> ch_SCAS,
        SymChannel@( CAS, C )<Object> ch_CASC
    ){
        this.ch_CS = ch_CS;
        this.ch_SCAS = ch_SCAS;
        this.ch_CASC = ch_CASC;
    }

    public void auth( Client@C client, Service@S service, Authenticator@CAS authenticator ) {
        Creds@CAS x =                      client.creds();
        if( authenticator.valid(x) ){
            

            Token@C t =                    service.newToken();
        }
        else{
            

            if( client.again() ){
                

                auth( client, service, authenticator );
            }
            else{
                

            }
        }
    }
}