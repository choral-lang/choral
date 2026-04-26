package Misc.SSOWithRetryMerge;

import choral.channels.SymChannel;

enum Validity@R { TOKEN, INVALID, RETRY, ERROR }

public class SSOWithRetryMerge@( C, S, CAS ) {
	SymChannel@( C, S )< Object > ch_CS;
	SymChannel@( S, CAS )< Object > ch_S_CAS;
	SymChannel@( CAS, C )< Object > ch_CAS_C;

	public void auth() {
		if( true@CAS ) {
			ch_CAS_C.< Validity >select( Validity@CAS.TOKEN );
			ch_S_CAS.< Validity >select( Validity@CAS.TOKEN );
		} else {
			ch_CAS_C.< Validity >select( Validity@CAS.INVALID );
			if( true@C ) {
				ch_CAS_C.< Validity >select( Validity@C.RETRY );
				ch_S_CAS.< Validity >select( Validity@CAS.RETRY );
				auth();
			} else {
				ch_CAS_C.< Validity >select( Validity@C.ERROR );
				ch_S_CAS.< Validity >select( Validity@CAS.ERROR );
			}
		}
	}
}
