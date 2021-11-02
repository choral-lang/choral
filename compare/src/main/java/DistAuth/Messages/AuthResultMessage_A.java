package DistAuth.Messages;

import DistAuth.DistAuthUtils.AuthResult_A;

public class AuthResultMessage_A implements Message {

	private final AuthResult_A result;

	public AuthResultMessage_A( AuthResult_A result ) {
		this.result = result;
	}

	public AuthResult_A result() {
		return result;
	}
}
