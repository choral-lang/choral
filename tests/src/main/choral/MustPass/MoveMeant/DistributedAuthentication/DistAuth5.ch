package choral.MustPass.MoveMeant.DistributedAuthentication;

import choral.MustPass.MoveMeant.DistributedAuthentication.AuthResult;
import choral.MustPass.MoveMeant.DistributedAuthentication.utils.AuthToken;
import choral.MustPass.MoveMeant.DistributedAuthentication.utils.Base64_Encoder;
import choral.MustPass.MoveMeant.DistributedAuthentication.utils.ClientRegistry;
import choral.MustPass.MoveMeant.DistributedAuthentication.utils.Credentials;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import choral.channels.SymChannel;
import choral.MustPass.MoveMeant.DistributedAuthentication.EnumBoolean;

public class DistAuth5@( Client, Service, S1, S2, IP ){
	private SymChannel@( Client, IP )< Object > ch_Client_IP;
	private SymChannel@( Service, IP )< Object > ch_Service_IP;
	private SymChannel@( S1, IP )< Object > ch_s1;
	private SymChannel@( S2, IP )< Object > ch_s2;

	public DistAuth5(
		SymChannel@( Client, IP )< Object > ch_Client_IP,
		SymChannel@( Service, IP )< Object > ch_Service_IP,
		SymChannel@( S1, IP )< Object > ch_s1,
		SymChannel@( S2, IP )< Object > ch_s2
	){
		this.ch_Client_IP = ch_Client_IP;
		this.ch_Service_IP = ch_Service_IP;
		this.ch_s1 = ch_s1;
		this.ch_s2 = ch_s2;
	}

	private String@Client calcHash( String@Client salt, String@Client pwd ){
        String@Client salt_and_pwd = salt + pwd;
		try {
			MessageDigest@Client md = MessageDigest@Client.getInstance( "SHA3-256"@Client );
			return salt_and_pwd.getBytes( StandardCharsets@Client.UTF_8 )
			>> md::digest
			>> Base64_Encoder@Client::encodeToString;
		} catch ( NoSuchAlgorithmException@Client e ) {
			e.printStackTrace();
			return "Algorithm not found"@Client;
		}
	}

	public AuthResult@( Client, Service ) authenticate( Credentials@Client credentials ) {
		String@Client salt = credentials.username
			
			>> ClientRegistry@IP::getSalt
			;
		Boolean@IP valid = calcHash( salt, credentials.password )
			
			>> ClientRegistry@IP::check;
		if( valid ){
			



			AuthToken@IP t = AuthToken@IP.create();
			return new AuthResult@( Client, Service )(
				t,
				t
			);
		} else {
			



			return new AuthResult@( Client, Service )();
		}
	}
}
