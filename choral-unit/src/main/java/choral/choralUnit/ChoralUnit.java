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

package choral.choralUnit;

import choral.annotations.Choreography;
import choral.choralUnit.annotations.Test;
import io.github.classgraph.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ChoralUnit {

	public static void main( String[] args ) {

		String target;

		if( args[ 0 ] != null && args[ 0 ].length() > 0 ) {
			target = args[ 0 ];
		} else {
			throw new ChoralUnitException(
					"ChoralUnit must be launched with a target, e.g., 'ChoralUnit MyClass'" );
		}

		ScanResult scanResult = new ClassGraph().enableMethodInfo().enableClassInfo().enableAnnotationInfo().scan();

		ClassInfoList classes = scanResult.getClassesWithAnnotation( Choreography.class.getName() )
				.filter( c -> c.getAnnotationInfo( Choreography.class.getName() )
						.getParameterValues().getValue( "name" ).equals( target ) );

		if( classes.size() == 0 ) {
			throw new ChoralUnitException( "Found 0 classes belonging to " + target );
		}

		MethodInfoList methods = classes.get( 0 ).getMethodInfo().filter(
				m -> m.hasAnnotation( Test.class.getName() ) );

		// Thread exceptions are collected in order to rethrow them from the main thread.
		// This enabled automated unit tests, either directly from Java,
		// or by inspecting the return code when running from the command line.
		ArrayList<Throwable> threadExceptions = new ArrayList<>();

		for( MethodInfo method : methods ) {
			List< Thread > threadList = new ArrayList<>();
			for( ClassInfo cls : classes ) {
				threadList.add(
						new Thread( () -> {
							try {
								Method classMethod = Class.forName( cls.getName() ).getMethod(
										method.getName() );
								if( Modifier.isStatic( classMethod.getModifiers() ) ) {
									Class.forName( cls.getName() ).getMethod(
											method.getName() ).invoke(
											Class.forName( cls.getName() ) );
								} else {
									throw new ChoralUnitException( "Test method " + method.getName()
											+ " in class " + cls.getName()
											+ " is not static. ChoralUnit method tests must all be static." );
								}
							} catch( ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e ) {
								e.printStackTrace();
								threadExceptions.add(e);
							}
						} )
				);
			}

			UncaughtExceptionHandler exceptionHandler = (thread, exception) -> {
				threadExceptions.add(exception);
			};
			threadList.forEach(t -> t.setUncaughtExceptionHandler(exceptionHandler));
	
			threadList.forEach(Thread::start);
			threadList.forEach(t -> {
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
	
			if (!threadExceptions.isEmpty())
				throw new RuntimeException(threadExceptions.get(0));
		}

	}

}
