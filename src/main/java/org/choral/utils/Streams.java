/*
 * Copyright (C) 2019-2020 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 * Copyright (C) 2019-2020 by Fabrizio Montesi <famontesi@gmail.com>
 * Copyright (C) 2019-2020 by Marco Peressotti <marco.peressotti@gmail.com>
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

package org.choral.utils;

import org.choral.Choral;

import java.util.function.Consumer;
import java.util.function.Function;

public class Streams {
	@FunctionalInterface
	public interface CheckedFunction< T, R > {
		R apply( T t ) throws Exception;
	}

	@FunctionalInterface
	public interface CheckedConsumer< T > {
		void apply( T t ) throws Exception;
	}

	public static class WrappedException extends RuntimeException {
		public WrappedException( Throwable cause ) {
			super( cause );
		}
	}

	public static < T, R > Function< T, R > wrapFunction(
			CheckedFunction< T, R > checkedFunction
	) {
		return t -> {
			try {
				return checkedFunction.apply( t );
			} catch( Exception e ) {
				throw new WrappedException( e );
			}
		};
	}

	public static < T > Consumer< T > wrapConsumer( CheckedConsumer< T > checkedConsumer ) {
		return t -> {
			try {
				checkedConsumer.apply( t );
			} catch( Exception e ) {
				throw new WrappedException( e );
			}
		};
	}

	public static < T > Consumer< T > skip() {
		return x -> {
		};
	}
}
