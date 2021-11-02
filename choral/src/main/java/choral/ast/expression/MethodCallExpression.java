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

package choral.ast.expression;

import choral.ast.Name;
import choral.ast.Node;
import choral.ast.Position;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.ChoralVisitorInterface;
import choral.ast.visitors.MergerInterface;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.exceptions.ChoralException;
import choral.types.Member;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A method call on an object of the shape a( b ) where
 * a is the method name
 * b is the list of arguments
 */

public class MethodCallExpression extends InvocationExpression {

	private final Name name;
	private final Set< WorldArgument > epp_worlds = new HashSet<>();

	public MethodCallExpression(
			final Name name, final List< Expression > arguments,
			List< TypeExpression > typeArguments
	) {
		super( arguments, typeArguments );
		this.name = name;
	}

	public MethodCallExpression(
			final Name name, final List< Expression > arguments,
			List< TypeExpression > typeArguments, final Position position
	) {
		super( arguments, typeArguments, position );
		this.name = name;
	}

	public boolean isSelect() {
		return ( methodAnnotation != null ) && methodAnnotation.higherCallable().isSelectionMethod();
	}

	private Member.GroundMethod methodAnnotation;

	public Optional< ? extends Member.GroundMethod > methodAnnotation() {
		return Optional.ofNullable( methodAnnotation );
	}

	public void setMethodAnnotation( Member.GroundMethod methodAnnotation ) {
		this.methodAnnotation = methodAnnotation;
	}


	public Set< WorldArgument > epp_worlds() {
		return epp_worlds;
	}

	public Name name() {
		return name;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public < R, T extends Node > R merge( MergerInterface< R > m, T n ) {
		try {
			return m.merge( this, ( this.getClass().cast( n ) ) );
		} catch( ClassCastException e ) {
			throw new ChoralException(
					this.position().line() + ":"
							+ this.position().column() + ":"
							+ "error: Could not merge \n" + new PrettyPrinterVisitor().visit(
							this ) + "\n with " + n.getClass().getSimpleName() );
		}
	}
}
