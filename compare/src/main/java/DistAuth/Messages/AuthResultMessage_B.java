package DistAuth.Messages;

import DistAuth.DistAuthUtils.AuthResult_B;

public class AuthResultMessage_B implements Message {

	private final AuthResult_B result;

	public AuthResultMessage_B( AuthResult_B result ) {
		this.result = result;
	}

	public AuthResult_B result() {
		return result;
	}
}
