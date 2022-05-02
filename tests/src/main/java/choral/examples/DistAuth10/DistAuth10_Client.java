package choral.examples.DistAuth10;

import choral.examples.AuthResult.AuthResult_A;
import choral.examples.DistAuthUtils.AuthToken;
import choral.examples.DistAuthUtils.Base64_Encoder;
import choral.examples.DistAuthUtils.Credentials;
import choral.lang.Unit;
import choral.DistAuth.EnumBoolean;
import java.security.NoSuchAlgorithmException;
import choral.runtime.TLSChannel.TLSChannel_A;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class DistAuth10_Client {
	private TLSChannel_A < Object > ch_Client_IP;

	public DistAuth10_Client( TLSChannel_A < Object > ch_Client_IP, Unit ch_Service_IP, Unit ch_s1, Unit ch_s2, Unit ch_s3, Unit ch_s4, Unit ch_s5, Unit ch_s6, Unit ch_s7 ) {
		this( ch_Client_IP );
	}
	
	public DistAuth10_Client( TLSChannel_A < Object > ch_Client_IP ) {
		this.ch_Client_IP = ch_Client_IP;
	}

	private String calcHash( String salt, String pwd ) {
		String salt_and_pwd = salt + pwd;
		try {
			MessageDigest md = MessageDigest.getInstance( "SHA3-256" );
			return Base64_Encoder.encodeToString( md.digest( salt_and_pwd.getBytes( StandardCharsets.UTF_8 ) ) );
		}
		catch ( NoSuchAlgorithmException e ) { 
			e.printStackTrace();
			return "Algorithm not found";
		}
	}
	
	public AuthResult_A authenticate( Credentials credentials ) {
		String salt = ch_Client_IP.< String >com( ch_Client_IP.< String >com( credentials.username ) );
		ch_Client_IP.< String >com( calcHash( salt, credentials.password ) );
		{
			switch( ch_Client_IP.< EnumBoolean >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case True -> {
					return new AuthResult_A( ch_Client_IP.< AuthToken >com( Unit.id ), Unit.id );
				}
				case False -> {
					return new AuthResult_A();
				}
			}
		}
	}

}
