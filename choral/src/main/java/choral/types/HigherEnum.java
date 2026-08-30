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
import choral.compiler.Diagnostics;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static choral.types.Modifier.*;

/** @see HigherDataType */
public final class HigherEnum extends HigherClass implements Enum {

	public HigherEnum(
			Package declarationContext,
			EnumSet< Modifier > modifiers,
			String identifier,
			World worldParameter
	) {
		super( declarationContext,
				setImplicitModifiers( modifiers ),
				identifier,
				List.of( worldParameter ),
				List.of() );
	}

	public HigherEnum(
			Package declarationContext,
			EnumSet< Modifier > modifiers,
			String identifier,
			World worldParameter,
			Node sourceCode
	) {
		super( declarationContext,
				setImplicitModifiers( modifiers ),
				identifier,
				List.of( worldParameter ),
				List.of(),
				sourceCode );
	}

	@Override
	public Variety variety() {
		return Variety.ENUM;
	}

	private static EnumSet< Modifier > setImplicitModifiers( EnumSet< Modifier > modifiers ) {
		modifiers.add( FINAL );
//        if (isNested) {
//            modifiers.add(STATIC);
//        }
		return modifiers;
	}

	@Override
	protected void assertModifiers( EnumSet< Modifier > modifiers ) {
		if( modifiers.contains( ABSTRACT ) ) {
			throw Diagnostics.enumHasAbstractModifier();
		}
		super.assertModifiers( modifiers );
	}

	@Override
	public GroundEnum applyTo( List< ? extends World > args ) {
		return applyTo( args, List.of() );
	}

	@Override
	public GroundEnum applyTo(
			List< ? extends World > worldArgs, List< ? extends HigherReferenceType > typeArgs
	) {
		return innerType().applySubstitution( getApplicationSubstitution( worldArgs, typeArgs ) );
	}

	@Override
	public HigherEnum partiallyApplyTo( List< ? extends HigherReferenceType > typeArgs ) {
		if( typeArgs.isEmpty() ) {
			return this;
		} else {
			throw Diagnostics.typeArgumentsWrongCount( 0, typeArgs.size() );
		}
	}

	private final Definition innerType = new Definition();

	@Override
	public Definition innerType() {
		return innerType;
	}

	public final class Definition extends HigherClass.Definition implements GroundEnum {

		private Definition() {
		}

		@Override
		public void setExtendedClass() {
			HigherClass t = (HigherClass) universe().specialType( Universe.SpecialTypeTag.ENUM );
			if( t == null ) {
				throw Diagnostics
						.symbolNotFound( Universe.SpecialTypeTag.ENUM.qualifiedName );
			}
			super.setExtendedClass( t.applyTo( this.worldArguments(),
					List.< HigherReferenceType >of( typeConstructor() ) ) );
			// super.extendedClass.checkInstantiation();
		}

		@Override
		public HigherEnum typeConstructor() {
			return HigherEnum.this;
		}

		@Override
		public void finaliseInterface() {
			if( isInterfaceFinalised() ) {
				return;
			}
			Member.HigherConstructor c = new Member.HigherConstructor(
					this,
					EnumSet.of( PRIVATE ),
					List.of() );
			c.innerCallable().finalise();
			addConstructor( c );
			super.finaliseInterface();
		}

		private final List< String > cases = new ArrayList<>( 20 );

		public Stream< String > cases() {
			return cases.stream();
		}

		public void addCase( String identifier ) {
			if( cases.contains( identifier ) ) {
				throw Diagnostics.enumCaseAlreadyDefined(
						identifier, typeConstructor().variety().labelSingular, typeConstructor() );
			}
			if( declaredFields().anyMatch( x -> x.identifier().equals( identifier ) ) ) {
				throw Diagnostics.enumCaseConflictsWithField(
						identifier, typeConstructor().variety().labelSingular, typeConstructor() );
			}
			addField( new Member.Field( this, identifier, EnumSet.of( STATIC, FINAL, PUBLIC ),
					this ) );
			cases.add( identifier );
		}

		@Override
		public void addField( Member.Field field ) {
			assert ( field.declarationContext() == this );
			if( cases.contains( field.identifier() ) ) {
				throw Diagnostics.enumFieldConflictsWithCase(
						field.identifier(), typeConstructor().variety().labelSingular,
						typeConstructor() );
			}
			super.addField( field );
		}

		private final HashMap< Substitution, GroundEnum > alphaIndex = new HashMap<>();

		@Override
		public GroundEnum applySubstitution( Substitution substitution ) {
			GroundEnum result = alphaIndex.get( substitution );
			if( result == null ) {
				result = new Proxy( substitution );
				alphaIndex.put( substitution, result );
			}
			return result;
		}

	}

	/** @see HigherDataType.Proxy */
	private class Proxy extends HigherClass.Proxy implements GroundEnum {

		private Proxy( Substitution substitution ) {
			super( substitution );
		}

		@Override
		public HigherEnum typeConstructor() {
			return HigherEnum.this;
		}

		@Override
		protected Definition definition() {
			return typeConstructor().innerType;
		}

		@Override
		public GroundEnum applySubstitution( Substitution substitution ) {
			return definition().applySubstitution( substitution().andThen( substitution ) );
		}

	}

}
