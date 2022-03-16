/*
 * Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 * Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 * Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package choral.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.FileNotFoundException;

public class SimpleTest {

	public static void main( String[] args ) throws FileNotFoundException {

		A o = new B();
		o.x = 12;
		Output out = new SimpleTest().send( o );
		new SimpleTest().recv( out );

	}

	Output send( A x ) throws FileNotFoundException {
		// SENDER
		Kryo k = new Kryo();
		k.register( A.class );
		k.register( x.getClass(), k.getSerializer( A.class ) );
//		Output output = new Output( new FileOutputStream( "file.bin" ) );
		Output output = new Output( 4096 );
		k.writeObject( output, x );
		output.close();
		return output;
	}

	void recv( Output out ) throws FileNotFoundException {
		// RECEIVER
		Kryo k = new Kryo();
		k.register( A.class );
//		Input input = new Input( new FileInputStream( "file.bin" ) );
		Input input = new Input( out.toBytes() );
		A recvObject = k.readObject( input, A.class );
		recvObject.m();
	}


	private static class A {
		int x;// = 256;

		void m() {
			System.out.println( "This is A and x is " + this.x );
		}
	}

	private static class B extends A {
		String x = "HELLO!";

		public void m() {
			System.out.println( "This is B and x is " + x );
		}
	}

}
