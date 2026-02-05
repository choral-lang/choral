package choral.MustPass.MoveMeant.ChannelsAsArgs;

import choral.MustPass.MoveMeant.ChannelsAsArgs.utils.Client;
import choral.annotations.Choreography;
import choral.channels.DiDataChannel_A;
import choral.channels.DiSelectChannel_A;
import choral.channels.SymChannel_A;
import choral.lang.Unit;
import java.util.ArrayList;

@Choreography( role = "First", name = "ChannelsAsArgs" )
public class ChannelsAsArgs_First {
	public static void fun( SymChannel_A < Object > ch_FirstSecond, SymChannel_A < Object > ch_SecondFirst, DiDataChannel_A < Object > diData, DiSelectChannel_A diSelect, Client c_First, Unit c_Second ) {
		fun( ch_FirstSecond, ch_SecondFirst, diData, diSelect, c_First );
	}
	
	private void helper( Integer in_First, Unit in_Second ) {
		
	}
	
	public static void fun( SymChannel_A < Object > ch_FirstSecond, SymChannel_A < Object > ch_SecondFirst, DiDataChannel_A < Object > diData, DiSelectChannel_A diSelect, Client c_First ) {
		ArrayList < Integer > list1_First = new ArrayList < Integer >();
		ch_SecondFirst.< ArrayList < Integer > >com( list1_First );
		ArrayList < ArrayList < Integer > > list2_First = new ArrayList < ArrayList < Integer > >();
		ch_SecondFirst.< ArrayList < ArrayList < Integer > > >com( list2_First );
		Integer i_First = 0;
		c_First.fun0();
		c_First.fun_in( i_First );
		Integer dependencyAtFirst_1781800167 = ch_SecondFirst.< Integer >com( Unit.id );
		c_First.fun_in( dependencyAtFirst_1781800167 );
		c_First.fun_in( c_First.fun_out() );
		ch_SecondFirst.< Integer >com( c_First.fun_out() );
		Integer dependencyAtFirst_1091831550 = ch_SecondFirst.< Integer >com( Unit.id );
		ch_SecondFirst.< Integer >com( c_First.fun_in_out( dependencyAtFirst_1091831550 ) );
		Integer dependencyAtFirst_1363977391 = ch_SecondFirst.< Integer >com( Unit.id );
		c_First.fun_in( dependencyAtFirst_1363977391 );
		String dependencyAtFirst_832443146 = ch_SecondFirst.< String >com( Unit.id );
		c_First.fun_in( dependencyAtFirst_832443146 );
		helper( i_First, Unit.id );
		helper( 0, Unit.id );
	}

}
