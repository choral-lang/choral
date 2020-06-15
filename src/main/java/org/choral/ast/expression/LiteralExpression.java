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

package org.choral.ast.expression;

import org.choral.ast.Node;
import org.choral.ast.Position;
import org.choral.ast.type.WorldArgument;
import org.choral.ast.visitors.ChoralVisitorInterface;
import org.choral.ast.visitors.MergerInterface;
import org.choral.exceptions.ChoralException;

import java.util.Collections;
import java.util.Set;

/**
 * A literal of the shape.
 * Boolean: true
 * Integer: 1
 * Double: 3.14
 * String: "Hello World!"
 */

public abstract class LiteralExpression< T > extends Expression {

	private final T content;
	private WorldArgument world;

	protected LiteralExpression( final T content, final WorldArgument world ) {
		this.content = content;
		this.world = world;
	}

	protected LiteralExpression(
			final T content, final WorldArgument world, final Position position
	) {
		super( position );
		this.content = content;
		this.world = world;
	}

	public T content() {
		return content;
	}

	public final WorldArgument world() {
		return world;
	}

	public void setWorldArgument( final WorldArgument w ) {
		this.world = w;
	}

	public final Set< WorldArgument > epp_worlds() {
		return world != null ? Set.of( world() ) : Collections.emptySet();
	}

	public static class BooleanLiteralExpression extends LiteralExpression< Boolean > {

		public BooleanLiteralExpression( final Boolean content, final WorldArgument world ) {
			super( content, world );
		}

		public BooleanLiteralExpression(
				final Boolean content, final WorldArgument world, final Position position
		) {
			super( content, world, position );
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
						"Could not merge " + this.getClass().getSimpleName() + " with " + n.getClass().getSimpleName() );
			}
		}

		@Override
		public int hashCode() {
			return this.content().hashCode();
		}

		@Override
		public boolean equals( Object obj ) {
			if( obj instanceof BooleanLiteralExpression ) {
				return ( (BooleanLiteralExpression) obj ).content().equals( this.content() );
			}
			return false;
		}

	}

	public static class IntegerLiteralExpression extends LiteralExpression< Integer > {

		public IntegerLiteralExpression( final Integer content, final WorldArgument world ) {
			super( content, world );
		}

		public IntegerLiteralExpression(
				final Integer content, final WorldArgument world, final Position position
		) {
			super( content, world, position );
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
						"Could not merge " + this.getClass().getSimpleName() + " with " + n.getClass().getSimpleName() );
			}
		}

		@Override
		public int hashCode() {
			return this.content().hashCode();
		}

		@Override
		public boolean equals( Object obj ) {
			if( obj instanceof IntegerLiteralExpression ) {
				return ( (IntegerLiteralExpression) obj ).content().equals( this.content() );
			}
			return false;
		}

	}

	public static class DoubleLiteralExpression extends LiteralExpression< Double > {

		public DoubleLiteralExpression( final Double content, final WorldArgument world ) {
			super( content, world );
		}

		public DoubleLiteralExpression(
				final Double content, final WorldArgument world, final Position position
		) {
			super( content, world, position );
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
						"Could not merge " + this.getClass().getSimpleName() + " with " + n.getClass().getSimpleName() );
			}
		}

		@Override
		public int hashCode() {
			return this.content().hashCode();
		}

		@Override
		public boolean equals( Object obj ) {
			if( obj instanceof DoubleLiteralExpression ) {
				return ( (DoubleLiteralExpression) obj ).content().equals( this.content() );
			}
			return false;
		}

	}

	public static class StringLiteralExpression extends LiteralExpression< String > {

		public StringLiteralExpression( final String content, final WorldArgument world ) {
			super( content, world );
		}

		public StringLiteralExpression(
				final String content, final WorldArgument world, final Position position
		) {
			super( content, world, position );
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
						"Could not merge " + this.getClass().getSimpleName() + " with " + n.getClass().getSimpleName() );
			}
		}

		@Override
		public int hashCode() {
			return this.content().hashCode();
		}

		@Override
		public boolean equals( Object obj ) {
			if( obj instanceof StringLiteralExpression ) {
				return ( (StringLiteralExpression) obj ).content().equals( this.content() );
			}
			return false;
		}
	}

}
