package choral.MustPass.MoveMeant.ChannelsAsArgs;

import choral.MustPass.MoveMeant.ChannelsAsArgs.utils.Client;
import choral.annotations.Choreography;
import choral.channels.DiDataChannel_B;
import choral.channels.DiSelectChannel_B;
import choral.channels.SymChannel_B;
import choral.lang.Unit;
import java.util.ArrayList;

@Choreography( role = "Second", name = "ChannelsAsArgs" )
public class ChannelsAsArgs_Second {
	public static void fun( SymChannel_B < Object > ch_FirstSecond, SymChannel_B < Object > ch_SecondFirst, DiDataChannel_B < Object > diData, DiSelectChannel_B diSelect, Unit c_First, Client c_Second ) {
		fun( ch_FirstSecond, ch_SecondFirst, diData, diSelect, c_Second );
	}
	
	private void helper( Unit in_First, Integer in_Second ) {
		
	}
	
	public static void fun( SymChannel_B < Object > ch_FirstSecond, SymChannel_B < Object > ch_SecondFirst, DiDataChannel_B < Object > diData, DiSelectChannel_B diSelect, Client c_Second ) {
		ArrayList < Integer > dependencyAtSecond_2102990885 = ch_SecondFirst.< ArrayList < Integer > >com( Unit.id );
		ArrayList < Integer > list1_Second = dependencyAtSecond_2102990885;
		ArrayList < ArrayList < Integer > > dependencyAtSecond_129097015 = ch_SecondFirst.< ArrayList < ArrayList < Integer > > >com( Unit.id );
		ArrayList < ArrayList < Integer > > list2_Second = dependencyAtSecond_129097015;
		Integer i_Second = 0;
		ch_SecondFirst.< Integer >com( i_Second );
		Integer dependencyAtSecond_899440059 = ch_SecondFirst.< Integer >com( Unit.id );
		c_Second.fun_in( dependencyAtSecond_899440059 );
		ch_SecondFirst.< Integer >com( c_Second.fun_out() );
		Integer dependencyAtSecond_1648356574 = ch_SecondFirst.< Integer >com( Unit.id );
		ch_SecondFirst.< Integer >com( c_Second.fun_in_out( dependencyAtSecond_1648356574 ) );
		ch_SecondFirst.< String >com( c_Second.price.currency );
		helper( Unit.id, i_Second );
		helper( Unit.id, 0 );
	}

}
