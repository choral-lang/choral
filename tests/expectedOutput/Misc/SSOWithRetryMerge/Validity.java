package Misc.SSOWithRetryMerge;

import choral.annotations.Choreography;

@Choreography( role = "R", name = "Validity" )
enum Validity {
	TOKEN,
	INVALID,
	RETRY,
	ERROR
}
