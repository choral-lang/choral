package choral.MustPass.MoveMeant.PingPong;

import choral.annotations.Choreography;
import choral.channels.DiSelectChannel_A;
import choral.channels.DiSelectChannel_B;
import java.lang.Thread;

@Choreography( role = "A", name = "PingPong" )
public class PingPong_A {
	public static void signal( DiSelectChannel_A ch_AB, DiSelectChannel_B ch_BA ) {
		try {
			Thread.sleep( 1000 );
			PingPong_B.signal( ch_BA, ch_AB );
		}
		catch ( InterruptedException e ) { 
			System.out.println( "Interrupted" );
		}
	}

}
