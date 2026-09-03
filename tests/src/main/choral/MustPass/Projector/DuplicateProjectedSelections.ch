package Projector.DuplicateProjectedSelections;

import choral.channels.SymChannel;

enum Choice@A { STRING, INTEGER, BOOLEAN }

class DuplicateProjectedSelections@( A, B ) {
    SymChannel@( A, B )< Object > channel;
    int@B result;

    DuplicateProjectedSelections( SymChannel@( A, B )< Object > channel ) {
        this.channel = channel;
    }

    void choose( String@A value ) {
        Choice@A.STRING >> channel::< Choice >select;
        result = 1@B;
    }

    void choose( Integer@A value ) {
        Choice@A.INTEGER >> channel::< Choice >select;
        result = 2@B;
    }

    void choose( boolean@A value ) {
        Choice@A.BOOLEAN >> channel::< Choice >select;
        result = 3@B;
    }

    void testSelections() {
        choose( "string"@A );
        choose( Integer@A.valueOf( 2@A ) );
        choose( true@A );
    }
}
