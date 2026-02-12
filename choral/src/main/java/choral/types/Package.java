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

package choral.types;

import choral.exceptions.StaticVerificationException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

/**
 * This class represents a package, like `java.util` or `com.example.foo`. It keeps track of
 * all packages and types declared inside it. For example, `java.util` is a Package declared
 * in the `java` package, and `java.util.Collection` is a type declared in the `java.util` Package.
 */
public class Package {

	private Package( Universe universe ) {
		this.identifier = "";
		this.declarationContext = null;
		this.universe = universe;
	}

	private Package( Package declarationContext, String identifier ) {
		this.declarationContext = declarationContext;
		this.universe = declarationContext.universe;
		this.identifier = identifier;
	}

	static Package createNewRoot( final Universe universe ) {
		return new Package( universe ) {
			@Override
			public boolean isRoot() {
				return true;
			}

			@Override
			public Package declarationContext() {
				throw new UnsupportedOperationException();
			}
		};
	}

	private final Universe universe;

	public Universe universe() {
		return universe;
	}

	public boolean isRoot() {
		return false;
	}

	public final Package root() {
		return ( isRoot() ) ? this : declarationContext().root();
	}

	private final Package declarationContext;

	/** Returns the parent Package in which this Package was declared. For example, the `java`
	 * package is the declaration context of the `java.util` package.
	 */
	public Package declarationContext() {
		return this.declarationContext;
	}

	private final String identifier;

	public String identifier() {
		return identifier( false );
	}

	public String identifier( boolean qualified ) {
		if( qualified && !declarationContext().isRoot() ) {
			return declarationContext().identifier( true ) + "." + identifier;
		} else {
			return this.identifier;
		}
	}

	@Override
	public String toString() {
		return identifier( true );
	}

	private final HashMap< String, Package > declaredPackages = new HashMap<>( 100 );

	public Optional< Package > declaredPackage( String name ) {
		return Optional.ofNullable( declaredPackages.get( name ) );
	}

	public Collection< Package > declaredPackages() {
		return declaredPackages.values();
	}

	public Package declarePackage( String path ) {
		String[] names = path.split( "\\." );
		Package pkg = this;
		for( String name : names ) {
			Optional< Package > x = pkg.declaredPackage( name );
			if( x.isEmpty() ) {
				if( declaredType( name ).isPresent() ) {
					throw new StaticVerificationException(
							"Duplicate declaration for '" + name + "'" );
				}
				Package y = new Package( pkg, name );
				pkg.declaredPackages.put( name, y );
				pkg = y;
			} else {
				pkg = x.get();
			}
		}
		return pkg;
	}

	private final HashMap< String, HigherClassOrInterface > declaredTypes = new HashMap<>( 100 );

	public Collection< HigherClassOrInterface > declaredTypes() {
		return Collections.unmodifiableCollection( declaredTypes.values() );
	}

	public final Optional< HigherClassOrInterface > declaredType( String name ) {
		return Optional.ofNullable( declaredTypes.get( name ) );
	}

	final void registerDeclaredType( HigherClassOrInterface type ) {
		assert ( type.declarationContext() == this );
		if( declaredTypes.containsKey( type.identifier() ) || declaredPackages.containsKey(
				type.identifier() ) ) {
			throw StaticVerificationException.of( "Duplicate declaration for '" + type.identifier()
					+ ( ( isRoot() ) ? "'" : "' in '" + this + "'" ), type.sourceCode() );
		}
		// System.out.println(type.variety().labelSingular + " '" + type + "' declared"); // DEBUG
		declaredTypes.put( type.identifier(), type );
	}
}
