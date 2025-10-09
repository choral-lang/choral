package choral.examples.TestSwitch;

import choral.annotations.Choreography;
import choral.channels.*;
import choral.lang.Unit;

@Choreography( role = "B", name = "TestSwitch" )
class TestSwitch_B {
	int m( SymChannel_B < Object > c ) {
		{
			switch( c.< C >select( Unit.id ) ){
				case LEFT -> {
					return 5;
				}
				case RIGHT -> {
					{
						switch( c.< C >select( Unit.id ) ){
							case RIGHT -> {
								return 6;
							}
							case LEFT -> {
								return 7;
							}
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
							}
						}
					}
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}

}
