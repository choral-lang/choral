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

package name_mangling.Utils;

import choral.ast.Name;
import choral.ast.type.WorldArgument;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Utils {

	public static void main ( String[] args ) {
		ArrayList< WorldArgument > worlds = new ArrayList<>();
		worlds.add( new WorldArgument( new Name( "A" ) ) );
		worlds.add( new WorldArgument( new Name( "B" ) ) );
		worlds.add( new WorldArgument( new Name( "C" ) ) );
//		worlds.add( new WorldArgument( new Name( "D" ) ) );
//		worlds.add( new WorldArgument( new Name( "E" ) ) );

		ArrayList< Integer > positions = new ArrayList<>();
		AtomicInteger i = new AtomicInteger( 0 );
		worlds.forEach( w -> positions.add( i.addAndGet( 1 ) ) );
		Set< List< Integer > > permutations = getPermutations( positions );
		Set< Map< WorldArgument, Integer > > pies = getPies( worlds, permutations );
		System.out.println(
				pies.stream().map(
						m -> m.entrySet().stream().map( ( e ) ->
							e.getKey().name().identifier() + " -> " + e.getValue()
						).collect( Collectors.joining( ", " ) )
				).collect( Collectors.joining( "\n" ) )
		);
	}

	private static Set< Map< WorldArgument, Integer > > getPies ( ArrayList< WorldArgument > worlds, Set< List< Integer > > permutations ) {
		return permutations.stream().map( p ->
				p.stream()
						.map( n -> new AbstractMap.SimpleEntry< WorldArgument, Integer >( worlds.get( p.indexOf( n ) ), n ) )
						.collect( Collectors.toMap( AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue ) )
		).collect( Collectors.toSet() );
	}

	public static Set< List< Integer > > getPermutations ( List< Integer > l ) {
		if ( l.size() == 0 ) {
			HashSet< List< Integer > > s = new HashSet<>();
			s.add( new ArrayList<>() );
			return s;
		} else {
			return l.stream().flatMap( e -> {
				ArrayList< Integer > _l = new ArrayList< Integer >( l );
				_l.remove( e );
				return getPermutations( _l ).stream()
						.peek( cl -> cl.add( 0, e ) );
			} ).collect( Collectors.toSet() );
		}

	}


}
