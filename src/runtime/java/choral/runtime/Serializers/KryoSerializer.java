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

package choral.runtime.Serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;

public final class KryoSerializer implements ChoralSerializer< Object, ByteBuffer > {
	private static final KryoSerializer INSTANCE = new KryoSerializer();
	private static final Kryo kryo = new Kryo();
	private static final Set< Integer > registeredClasses = new HashSet<>();
	private boolean preloadDone = false;

	static {
		kryo.setInstantiatorStrategy(
				new DefaultInstantiatorStrategy(
						// <-- the default initiator uses the empty-params constructor
						new StdInstantiatorStrategy()
						// <-- if the default fails, use JVM APIs to create
						// an instance of a class without calling any constructor at all.
				)
		);
	}

	private KryoSerializer() {
	}

	public static KryoSerializer getInstance() {
		return INSTANCE;
	}

	private static void register( Class c ) {
		kryo.register( c, c.hashCode() );
		registeredClasses.add( c.hashCode() );
	}

	private static void preload() {
		if( getInstance().preloadDone ) {
			return;
		} else {
			getInstance().preloadDone = true;
		}
		try( ScanResult scanResult = new ClassGraph().enableAllInfo().scan() ) {
			scanResult.getAllClasses()
					.filter( c -> c.hasAnnotation( KryoSerializable.class.getName() ) )
					.filter( c -> !registeredClasses.contains( c.hashCode() ) )
					.forEach( c -> {
						try {
							KryoSerializer.register( Class.forName( c.getName() ) );
							Class< ? > _class = Class.forName( c.getName() );
							for( Field field : _class.getDeclaredFields() ) {
								KryoSerializer.register( field.getType() );
							}
						} catch( ClassNotFoundException e ) {
							e.printStackTrace();
						}
					} );
		}
	}

	@Override
	public < M > ByteBuffer fromObject( M o ) {
		preload();
		Output output = new Output( 4096 );
		if( o instanceof Optional ) {
			kryo.register( Optional.class );
			if( ( (Optional) o ).isPresent() ) {
				kryo.register( ( (Optional) o ).get().getClass() );
			}
		}
		kryo.writeClassAndObject( output, o );
		output.close();
		return ByteBuffer.wrap( output.getBuffer() );
	}

	@Override
	public < M > M toObject( ByteBuffer b ) {
		preload();
		Input input = new Input( b.array() );
		M o = (M) kryo.readClassAndObject( input );
		input.close();
		return o;
	}

}
