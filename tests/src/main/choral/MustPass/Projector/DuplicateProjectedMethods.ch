package Projector.DuplicateProjectedMethods;

import choral.channels.SymChannel;

class DuplicateProjectedMethods@( A, B ) {
    SymChannel@( A, B )< Object > channel;

    DuplicateProjectedMethods( SymChannel@( A, B )< Object > channel ) {
        this.channel = channel;
    }

    Integer@A identity( Integer@A value ) { return value; }
    int@A identity( int@A value ) { return value; }

    void accept( Object@A left, Object@A right ) {}
    void accept( int@A left, int@A right ) {}

    Object@B communicate( String@A value ) {
        return channel.< Object >com( value );
    }

    Object@B communicate( Integer@A value ) {
        return channel.< Object >com( value );
    }

    void testCommunication() {
        communicate( "hello"@A );
        communicate( Integer@A.valueOf( 42@A ) );
    }
}
