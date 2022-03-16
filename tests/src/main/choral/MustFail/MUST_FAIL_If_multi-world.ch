package choral.mustfail.if_multiworld;

import choral.channels.SymChannel;

enum Choice@Role { THEN, ELSE }

class If_MultiWorld@( A, B, C ){
static SymChannel@( A, B )< Boolean > ch1;
static SymChannel@( B, C )< Boolean > ch2;
static SymChannel@( C, A )< Boolean > ch3;

static void main(){
	// this should pass
	if( true@A && false@A || true@A ){
		ch1.< Choice >select( Choice@A.THEN );
		ch3.< Choice >select( Choice@A.THEN );
	} else {
		ch1.< Choice >select( Choice@A.ELSE );
		ch3.< Choice >select( Choice@A.ELSE );
	}
	// this should pass too
	if( ch1.< Boolean >com( true@B ) && false@A || true@A ){
		ch1.< Choice >select( Choice@A.THEN );
		ch3.< Choice >select( Choice@A.THEN );
	} else {
		ch1.< Choice >select( Choice@A.ELSE );
		ch3.< Choice >select( Choice@A.ELSE );
	}
	// this should not pass
	if( true@A && ch1.< Boolean >com( false@B ) || true@A ){
		ch1.< Choice >select( Choice@A.THEN );
		ch3.< Choice >select( Choice@A.THEN );
	} else {
		ch1.< Choice >select( Choice@A.ELSE );
		ch3.< Choice >select( Choice@A.ELSE );
	}
}


}
