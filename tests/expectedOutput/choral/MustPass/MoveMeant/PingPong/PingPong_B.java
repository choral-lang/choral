package choral.MustPass.MoveMeant.PingPong;

import choral.annotations.Choreography;
import choral.channels.DiSelectChannel_A;
import choral.channels.DiSelectChannel_B;

@Choreography( role = "B", name = "PingPong" )
public class PingPong_B {
	public static void signal( DiSelectChannel_B ch_AB, DiSelectChannel_A ch_BA ) {
		{
			System.out.println( "ping" );
			PingPong_A.signal( ch_BA, ch_AB );
		}
	}

}
