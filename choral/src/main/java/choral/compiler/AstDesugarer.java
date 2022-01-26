/*
 *   Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 *   Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 *   Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as
 *   published by the Free Software Foundation; either version 2 of the
 *   License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the
 *   Free Software Foundation, Inc.,
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package choral.compiler;

import choral.ast.CompilationUnit;
import choral.ast.Node;
import choral.ast.statement.Statement;
import choral.ast.statement.VariableDeclarationStatement;
import choral.ast.visitors.ChoralVisitor;

import java.util.Collections;

public class AstDesugarer extends ChoralVisitor {

	private AstDesugarer() {
	}

	public static CompilationUnit desugar( Node n ) {
		if( n instanceof CompilationUnit ) {
			return (CompilationUnit) new AstDesugarer().visit( (CompilationUnit) n );
		} else {
			throw new UnsupportedOperationException(
					"desugaring only available from a CompilationUnit node" );
		}
	}

	@Override
	public Node visit( VariableDeclarationStatement n ) {
		if( n.variables().size() > 1 ) {
			VariableDeclarationStatement s = new VariableDeclarationStatement(
					Collections.singletonList( n.variables().get( n.variables().size() - 1 ) ),
					(Statement) visit( n.continuation() ),
					n.variables().get( n.variables().size() - 1 ).position()
			);
			for( int i = n.variables().size() - 2; i >= 0; i-- ) {
				s = new VariableDeclarationStatement(
						Collections.singletonList( n.variables().get( i ) ),
						s,
						n.variables().get( i ).position()
				);
			}
			return s;
		} else {
			return new VariableDeclarationStatement(
					n.variables(),
					(Statement) visit( n.continuation() ),
					n.position()
			);
		}
	}

//	@Override
//	public Node visit( IfStatement n ) {
//		Map< SwitchArgument, Statement > m = new HashMap<>();
//		m.put( new SwitchArgument.SwitchArgumentLabel( new Name( "True" ) ), ( Statement ) visit( n.ifBranch() ) );
//		m.put( new SwitchArgument.SwitchArgumentLabel(  new Name( "False" ) ), ( Statement ) visit( n.elseBranch() ) );
//		return new SwitchStatement( n.condition(), m, ( Statement ) visit( n.continuation() ) );
//	}
}
