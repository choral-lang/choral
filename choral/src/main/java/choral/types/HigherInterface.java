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

import choral.ast.Node;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static choral.types.Modifier.*;
import static choral.types.ModifierUtils.assertLegalModifiers;

public final class HigherInterface extends HigherClassOrInterface implements Interface {

	public HigherInterface(
			Package declarationContext,
			EnumSet< Modifier > modifiers,
			String identifier,
			List< World > worldsParameters,
			List< HigherTypeParameter > typeParameters
	) {
		super( declarationContext,
				setImplicitModifiers( modifiers ),
				identifier,
				worldsParameters,
				typeParameters );
	}

	public HigherInterface(
			Package declarationContext,
			EnumSet< Modifier > modifiers,
			String identifier,
			List< World > worldsParameters,
			List< HigherTypeParameter > typeParameters,
			Node sourceCode
	) {
		super( declarationContext,
				setImplicitModifiers( modifiers ),
				identifier,
				worldsParameters,
				typeParameters,
				sourceCode );
	}

	@Override
	public Variety variety() {
		return Variety.INTERFACE;
	}

	private static final EnumSet< Modifier > legalModifiers = EnumSet.of( PUBLIC, ABSTRACT,
			STATIC );

	@Override
	protected void assertModifiers( EnumSet< Modifier > modifiers ) {
		assertLegalModifiers( legalModifiers, modifiers, "for interfaces" );
		super.assertModifiers( modifiers );
	}

	private static EnumSet< Modifier > setImplicitModifiers( EnumSet< Modifier > modifiers ) {
		modifiers.add( ABSTRACT );
		return modifiers;
	}

	@Override
	public GroundInterface applyTo( List< ? extends World > args ) {
		return applyTo( args,
				typeParameters.stream().map( HigherTypeParameter::getRawType ).collect(
						Collectors.toList() ) );
	}

	@Override
	public GroundInterface applyTo(
			List< ? extends World > worldArgs, List< ? extends HigherReferenceType > typeArgs
	) {
		return innerType().applySubstitution( getApplicationSubstitution( worldArgs, typeArgs ) );
	}

	private final Definition innerType = new Definition();

	@Override
	public Definition innerType() {
		return innerType;
	}

	public void addImplicitMethodModifiers( EnumSet< Modifier > modifiers ) {
		modifiers.add( ABSTRACT );
		modifiers.add( PUBLIC );
	}

	public final class Definition extends HigherClassOrInterface.Definition
			implements GroundInterface {

		private Definition() {
		}

		@Override
		public HigherInterface typeConstructor() {
			return HigherInterface.this;
		}

		private final HashMap< Substitution, GroundInterface > alphaIndex = new HashMap<>();

		@Override
		public GroundInterface applySubstitution( Substitution substitution ) {
			GroundInterface result = alphaIndex.get( substitution );
			if( result == null ) {
				result = new Proxy( substitution );
				alphaIndex.put( substitution, result );
			}
			return result;
		}

		@Override
		public Stream< ? extends Member.Field > fields() {
			return Stream.concat( declaredFields(),
					extendedInterfaces().flatMap( x -> x.fields() ) );
		}

		@Override
		public Stream< ? extends Member.HigherMethod > methods() {
			return Stream.concat( declaredMethods(),
					extendedInterfaces().flatMap( x -> x.methods() ) );
		}

		public void addField( Member.Field field ) {
			throw new UnsupportedOperationException( "interfaces cannot have fields" );
		}

		public void addMethod( Member.HigherMethod method ) {
			assert ( method.isPublic() && method.isAbstract() );
//			if(!method.isPublic() || !method.isAbstract()){
//				throw new IllegalArgumentException("interface methods must be public and abstract");
//			}
			super.addMethod( method );
		}

	}

	private final class Proxy extends HigherClassOrInterface.Proxy implements GroundInterface {

		private Proxy( Substitution substitution ) {
			super( substitution );
		}

		@Override
		public HigherInterface typeConstructor() {
			return HigherInterface.this;
		}

		@Override
		protected Definition definition() {
			return typeConstructor().innerType();
		}

		@Override
		public GroundInterface applySubstitution( Substitution substitution ) {
			return definition().applySubstitution( substitution().andThen( substitution ) );
		}

	}

}
