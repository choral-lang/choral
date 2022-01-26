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

package choral.runtime.TLSByteChannel.tlschannel.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Access to NIO sun.misc.Cleaner, allowing caller to deterministically deallocate a given sun.nio.ch.DirectBuffer.
 */
public class DirectBufferDeallocator {

	private interface Deallocator {
		void free( ByteBuffer bb );
	}

	private static class Java8Deallocator implements Deallocator {

		/*
		 * Getting instance of cleaner from buffer (sun.misc.Cleaner)
		 */

		final Method cleanerAccessor;
		final Method clean;

		Java8Deallocator() {
			try {
				cleanerAccessor = Class.forName( "sun.nio.ch.DirectBuffer" ).getMethod( "cleaner",
						(Class< ? >[]) null );
				clean = Class.forName( "sun.misc.Cleaner" ).getMethod( "clean" );
			} catch( NoSuchMethodException | ClassNotFoundException t ) {
				throw new RuntimeException( t );
			}
		}

		@Override
		public void free( ByteBuffer bb ) {
			try {
				clean.invoke( cleanerAccessor.invoke( bb ) );
			} catch( IllegalAccessException | InvocationTargetException t ) {
				throw new RuntimeException( t );
			}
		}
	}

	private static class Java9Deallocator implements Deallocator {

		/*
		 * Clean is of type jdk.internal.ref.Cleaner, but this type is not accessible, as it is not exported by default.
		 * Using workaround through sun.misc.Unsafe.
		 */

		final Object unsafe;
		final Method invokeCleaner;

		Java9Deallocator() {
			try {
				Class< ? > unsafeClass = Class.forName( "sun.misc.Unsafe" );
				// avoiding getUnsafe methods, as it is explicitly filtered out from reflection API
				Field theUnsafe = unsafeClass.getDeclaredField( "theUnsafe" );
				theUnsafe.setAccessible( true );
				unsafe = theUnsafe.get( null );
				invokeCleaner = unsafeClass.getMethod( "invokeCleaner", ByteBuffer.class );
			} catch( NoSuchMethodException | ClassNotFoundException | IllegalAccessException | NoSuchFieldException t ) {
				throw new RuntimeException( t );
			}
		}

		@Override
		public void free( ByteBuffer bb ) {
			try {
				invokeCleaner.invoke( unsafe, bb );
			} catch( IllegalAccessException | InvocationTargetException t ) {
				throw new RuntimeException( t );
			}
		}

	}

	private final Deallocator deallocator;

	public DirectBufferDeallocator() {
		if( Util.getJavaMajorVersion() >= 9 ) {
			deallocator = new Java9Deallocator();
//            logger.debug("initialized direct buffer deallocator for java >= 9");
		} else {
			deallocator = new Java8Deallocator();
//            logger.debug("initialized direct buffer deallocator for java < 9");
		}
	}

	public void deallocate( ByteBuffer buffer ) {
		deallocator.free( buffer );
	}

}
