package choral.MustPass.MoveMeant.ChannelsAsArgs;

import choral.MustPass.MoveMeant.ChannelsAsArgs.utils.Client;
import choral.channels.SymChannel;
import choral.channels.DiDataChannel;
import choral.channels.DiSelectChannel;
import java.util.List;

import java.util.ArrayList;

enum Signal@R{ SIG }

public class ChannelsAsArgs@( First, Second ){

    public static void fun( 
        SymChannel@( First, Second )< Object > ch_FirstSecond, 
        SymChannel@( First, Second )< Object > ch_SecondFirst,
        DiDataChannel@( First, Second )< Object > diData,
        DiSelectChannel@( First, Second ) diSelect,
		Client@First c_First, 
		Client@Second c_Second
    ) {

		ArrayList@First<Integer> list1_First = new ArrayList@First<Integer>();
		ArrayList@Second<Integer> list1_Second = list1_First;
		
		ArrayList@First<ArrayList<Integer>> list2_First = new ArrayList@First<ArrayList<Integer>>();
		ArrayList@Second<ArrayList<Integer>> list2_Second = list2_First;
        Integer@First i_First = 0@First;
		Integer@Second i_Second = 0@Second;
		c_First.fun0();
		c_First.fun_in( i_First );
		c_First.fun_in( i_Second );
		// c_First.fun_in( 0@Second ); // illegal

		c_First.fun_in( c_First.fun_out() );
		// c_First.fun_in( c_Second.fun_out() );
		c_Second.fun_in( c_First.fun_out() );

		c_First.fun_in( c_Second.fun_in_out( c_First.fun_in_out( c_Second.fun_out() ) ) );
		c_First.fun_in( c_Second.price.currency );

		helper( i_First, i_Second );
		helper( 0@First, 0@Second );
    }

	private void helper(Integer@First in_First, Integer@Second in_Second){}
}