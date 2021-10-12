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

package choral.compiler.soloist;

import choral.ast.Node;
import choral.ast.body.Class;
import choral.ast.body.Enum;
import choral.ast.body.Interface;
import choral.ast.expression.Expression;
import choral.ast.expression.ScopedExpression;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.WorldArgument;
import choral.utils.Pair;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Utils {

	public static Pair< Expression, Expression > headAndTail( ScopedExpression e ) {
		if( e.scope() instanceof ScopedExpression ) {
			Pair< Expression, Expression > head = headAndTail( (ScopedExpression) e.scope() );
			return new Pair<>( head.left(),
					new ScopedExpression( head.right(), e.scopedExpression() ) );
		} else {
			return new Pair<>( e.scope(), e.scopedExpression() );
		}
	}

	static String getProjectionName(
			String name, WorldArgument world, List< WorldArgument > worlds
	) {
		return name + ( worlds.size() > 1 ? "_" + world.name().identifier() : "" );
	}

	static String getProjectionName(
			String name, WorldArgument world, List< WorldArgument > worlds,
			List< WorldArgument > referenceWorlds
	) {
		int worldIndex = worlds.indexOf( world );
		WorldArgument referenceWorld = referenceWorlds.get( worldIndex );
		return name + ( worlds.size() > 1 ? "_" + referenceWorld.name().identifier() : "" );
	}

	public static void warnIfWorldNotPresent(
			List< FormalWorldParameter > worlds, WorldArgument world, Node n
	) {
		String templateName;
		if( n instanceof Interface ) {
			templateName = "Interface " + ( (Interface) n ).name();
		} else if( n instanceof Class ) {
			templateName = "Class " + ( (Class) n ).name();
		} else if( n instanceof Enum ) {
			templateName = "Enum " + ( (Enum) n ).name();
		} else {
			throw new SoloistProjectorException(
					"method warnIfWorldNotPresent called on a node different from an Inteface, a Class, or an Enum" );
		}
		if( !worlds.stream().map( FormalWorldParameter::toWorldArgument ).collect(
				Collectors.toList() ).contains( world ) ) {
			System.out.println(
					"WARNING: compilation launched on world: '" + world + "' "
							+ "but missing in " + templateName

			);
		}
	}

	static < T > IfPresent< T > ifPresent( List< T > o ) {
		return new IfPresent<>( o );
	}

	public static class IfPresent< T > {

		private final Optional< T > o;

		private IfPresent( List< T > o ) {
			if( o == null || o.isEmpty() ) {
				this.o = Optional.empty();
			} else {
				this.o = Optional.of( o.get( 0 ) );
			}
		}

		T getOrElse( Supplier< T > e ) {
			if( e == null ) {
				throw new RuntimeException(
						"Both application and alternative functions must be not null" );
			}
			return o.orElseGet( e );
		}

	}

}
