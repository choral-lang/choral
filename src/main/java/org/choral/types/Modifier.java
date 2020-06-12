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

package org.choral.types;

import org.choral.utils.Formatting;
import org.choral.exceptions.StaticVerificationException;

import java.util.EnumSet;
import java.util.stream.Collectors;

import static org.choral.types.Modifier.*;
import static org.choral.types.Modifier.FINAL;

public enum Modifier {
	STATIC( "static" ),
	PUBLIC( "public" ),
	PROTECTED( "protected" ),
	PRIVATE( "private" ),
	FINAL( "final" ),
	ABSTRACT( "abstract" );

	public final String label;

	private Modifier( String label ) {
		this.label = label;
	}
}

class ModifierUtils {
	public static void assertAccessModifiers( EnumSet< Modifier > modifiers ) {
		EnumSet< Modifier > access = EnumSet.of( PUBLIC, PRIVATE, PROTECTED );
		access.removeAll( modifiers );
		if( access.size() < 2 ){
			EnumSet< Modifier > used = EnumSet.of( PUBLIC, PRIVATE, PROTECTED );
			used.remove( access );
			throw new StaticVerificationException( "illegal combination of modifiers " + prettyModifierList( used ) + "" );
		}
	}

	public static void assertLegalModifiers( EnumSet< Modifier > allowed, EnumSet< Modifier > toCheck, String where ) {
		EnumSet< Modifier > copy = EnumSet.copyOf( toCheck );
		copy.removeAll( allowed );
		if( copy.size() > 0 ){ // Illegal modifiers
			String ms = ( copy.size() > 1 ) ? "modifier " : "modifiers ";
			throw new StaticVerificationException( ms + prettyModifierList( copy ) + " not allowed " + where + "" );
		}
	}

	public static void assertIllegalCombinationOfModifiers( EnumSet< Modifier > modifiers, Modifier m1, Modifier m2 ) {
		if( modifiers.contains( m1 ) && modifiers.contains( m2 ) ){
			throw new StaticVerificationException( "illegal combination of modifiers '" + m1.label + "' and '" + m2.label + "'" );
		}
	}

	public static String prettyModifierList( EnumSet< Modifier > modifiers ) {
		return modifiers.stream().map( x -> "'" + x.label + "'" )
				.collect( Collectors.collectingAndThen( Collectors.toList(), Formatting.joiningOxfordComma() ) );
	}

	public static String prettyAccess( EnumSet< Modifier > modifiers ){
		String r = "package-private";
		for(Modifier m : EnumSet.of( PUBLIC, PRIVATE, PROTECTED )) {
			if(modifiers.contains( m )) {
				r = m.label;
				break;
			}
		}
		return r;
	}
}
