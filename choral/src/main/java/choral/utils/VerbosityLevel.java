package choral.utils;

public enum VerbosityLevel {
	ERRORS( -1 ),
	WARNINGS( 0 ),
	INFO( 1 ),
	DEBUG( 2 );

	final int value;

	VerbosityLevel( int value ) {
		this.value = value;
	}
}
