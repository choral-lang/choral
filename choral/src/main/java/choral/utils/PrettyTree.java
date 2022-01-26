/*
 *     Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 *     Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 *     Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU Library General Public License as
 *     published by the Free Software Foundation; either version 2 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU Library General Public
 *     License along with this program; if not, write to the
 *     Free Software Foundation, Inc.,
 *     59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package choral.utils;

import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;

import java.util.List;

public class PrettyTree {

	public static final String Eol = System.lineSeparator();


	public static String toPrettyTree( final Tree t, final List< String > ruleNames ) {
		return process( t, ruleNames, 0 );
	}

	private static String process( final Tree t, final List< String > ruleNames, int level ) {
		if( t.getChildCount() == 0 ) return Trees.getNodeText( t, ruleNames );
		StringBuilder sb = new StringBuilder();
		sb.append( lead( level ) );
		level++;
		String s = Trees.getNodeText( t, ruleNames );
		sb.append( s + ' ' );
		for( int i = 0; i < t.getChildCount(); i++ ) {
			sb.append( process( t.getChild( i ), ruleNames, level ) );
		}
		level--;
		sb.append( lead( level ) );
		return sb.toString();
	}

	private static String getIndents() {
		return " ";
	}

	private static String lead( int level ) {
		StringBuilder sb = new StringBuilder();
		if( level > 0 ) {
			sb.append( Eol );
			for( int cnt = 0; cnt < level; cnt++ ) {
				sb.append( getIndents() );
			}
		}
		return sb.toString();
	}


}
