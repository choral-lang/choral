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

import org.choral.ast.Node;
import org.choral.exceptions.StaticVerificationException;
import org.choral.utils.Formatting;

import java.util.*;
import java.util.stream.Collectors;

import static org.choral.types.Modifier.*;
import static org.choral.types.ModifierUtils.*;

public abstract class Member implements HasSource {

	protected Member(
			GroundClassOrInterface declarationContext,
			String identifier,
			Variety variety,
			EnumSet< Modifier > modifiers
	) {
		this( declarationContext, identifier, variety, modifiers, true );
	}

	protected Member(
			GroundClassOrInterface declarationContext,
			String identifier,
			Variety variety,
			EnumSet< Modifier > modifiers,
			boolean checkModifiers
	) {
		this.declarationContext = declarationContext;
		this.variety = variety;
		this.modifiers = EnumSet.copyOf( modifiers );
		this.identifier = identifier;
		if( checkModifiers ) {
			assertModifiers( modifiers );
		}
	}

	// source code position for error reporting
	private Node source;

	@Override
	public final Optional< Node > sourceCode() {
		return Optional.ofNullable( source );
	}

	@Override
	public final void setSourceCode( Node source ) {
		this.source = source;
	}

	private final String identifier;

	public String identifier() {
		return identifier;
	}

	private final EnumSet< Modifier > modifiers;

	protected void assertModifiers( EnumSet< Modifier > modifiers ) {
		assertAccessModifiers( modifiers );
		assertIllegalCombinationOfModifiers( modifiers, ABSTRACT, STATIC );
		assertIllegalCombinationOfModifiers( modifiers, ABSTRACT, PRIVATE );
		assertIllegalCombinationOfModifiers( modifiers, ABSTRACT, FINAL );
	}

	protected final EnumSet< Modifier > modifiers() {
		return modifiers;
	}

	public final boolean isAbstract() {
		return modifiers.contains( ABSTRACT );
	}

	public final boolean isPublic() {
		return modifiers.contains( PUBLIC );
	}

	public final boolean isPrivate() {
		return modifiers.contains( PRIVATE );
	}

	public final boolean isProtected() {
		return modifiers.contains( PROTECTED );
	}

	public final boolean isPackagePrivate() {
		return !isPublic() && !isProtected() && !isPrivate();
	}

	public final boolean isFinal() {
		return modifiers.contains( FINAL );
	}

	public final boolean isStatic() {
		return modifiers.contains( STATIC );
	}

	public final boolean isAccessibleFrom( GroundClassOrInterface context ) {
		/* DEBUG */
//		System.out.println("-- isAccessible");
//		System.out.println("  " + this);
//		System.out.println("  " + this.declarationContext + " == " +  context + " " + (this.declarationContext == context));
//		System.out.println("  " + this.isPublic());
//		System.out.println("  " + this.isProtected());
//		System.out.println("  " + this.isPrivate());
//		System.out.println("  " + this.isPackagePrivate());
//		System.out.println("-- result = " + (this.declarationContext == context || this.isPublic() || this.isProtected() || ( this.isPackagePrivate() &&
//				context.declarationPackage() == this.declarationContext().declarationPackage() )));
		return this.declarationContext == context || this.isPublic() || this.isProtected() || ( this.isPackagePrivate() &&
				context.declarationPackage() == this.declarationContext().declarationPackage() );
	}

	private final GroundClassOrInterface declarationContext;

	public GroundClassOrInterface declarationContext() {
		return declarationContext;
	}

	public enum Variety {
		FIELD( "field" ),
		METHOD( "method" ),
		CONSTRUCTOR( "constructor" );

		public final String label;

		private Variety( String label ) {
			this.label = label;
		}
	}

	private final Variety variety;

	public final Variety family() {
		return variety;
	}

	abstract Member applySubstitution( Substitution substitution );

	public static final class Field extends Member {

		private final GroundDataType type;

		public Field(
				GroundClassOrInterface declarationContext,
				String identifier,
				EnumSet< Modifier > modifiers,
				GroundDataType type
		) {
			super( declarationContext, identifier, Variety.FIELD, modifiers );
			this.type = type;
		}

		private Field(
				GroundClassOrInterface declarationContext,
				String identifier,
				EnumSet< Modifier > modifiers,
				GroundDataType type,
				boolean performCheks
		) {
			super( declarationContext, identifier, Variety.FIELD, modifiers, performCheks );
			this.type = type;
		}

		@Override
		protected void assertModifiers( EnumSet< Modifier > modifiers ) {
			if( modifiers.contains( ABSTRACT ) ) {
				throw new StaticVerificationException(
						"modifier 'abstract' not allowed for fields" );
			}
			super.assertModifiers( modifiers );
		}

		public GroundDataType type() {
			return type;
		}

		@Override
		Field applySubstitution( Substitution substitution ) {
			GroundDataType type = this.type().applySubstitution( substitution );
			GroundClassOrInterface declarationContext = this.declarationContext().applySubstitution(
					substitution );
			return new Field( declarationContext, identifier(), modifiers(), type, false );
		}
	}

	public static abstract class HigherCallable extends Member
			implements TypeParameterDeclarationContext {

		private final static String CONSTRUCTOR_NAME = "new";

		protected HigherCallable(
				GroundClassOrInterface declarationContext,
				String identifier,
				Variety variety,
				EnumSet< Modifier > modifiers,
				List< HigherTypeParameter > typeParameters,
				boolean performChecks
		) {
			super(
					declarationContext,
					identifier,
					variety,
					modifiers,
					performChecks
			);

			this.typeParameters = new ArrayList<>( typeParameters );
			if( performChecks ) {
				String[] names = new String[ typeParameters.size() ];
				int i = 0;
				for( HigherTypeParameter x : typeParameters ) {
					x.setDeclarationContext( this );
					for( int j = 0; j < i; j++ ) {
						if( names[ j ].equals( x.identifier() ) ) {
							throw StaticVerificationException.of(
									"duplicate type parameter '" + names[ j ] + "'",
									x.sourceCode() );
						}
					}
					names[ i++ ] = x.identifier();
				}
			}
		}

		private final ArrayList< HigherTypeParameter > typeParameters;

		@Override
		public List< ? extends HigherTypeParameter > typeParameters() {
			return Collections.unmodifiableList( typeParameters );
		}

		@Override
		public Optional< ? extends HigherTypeParameter > typeParameter( int index ) {
			if( 0 <= index && index < typeParameters.size() ) {
				return Optional.of( typeParameters.get( index ) );
			} else {
				return Optional.empty();
			}
		}

		@Override
		public Optional< ? extends HigherTypeParameter > typeParameter( String name ) {
			return typeParameters.stream().filter( x -> x.identifier().equals( name ) ).findAny();
		}

		public abstract GroundCallable applyTo( List< ? extends HigherReferenceType > typeArgs );

		protected final Substitution getApplicationSubstitution(
				List< ? extends HigherReferenceType > typeArgs
		) {
			if( typeArgs.size() != typeParameters.size() ) {
				throw new StaticVerificationException(
						"illegal type instantiation: expected " + typeParameters.size() + " type arguments but found " + typeArgs.size() );
			}
			Substitution substitution = new Substitution() {
				@Override
				public HigherReferenceType get( HigherTypeParameter placeHolder ) {
					int i = typeParameters.indexOf( placeHolder );
					return ( i == -1 ) ? placeHolder : typeArgs.get( i );
				}
			};
			for( HigherTypeParameter t : typeParameters ) {
				t.assertWithinBounds( substitution );
			}
			return substitution;
		}

		@Override
		abstract HigherCallable applySubstitution( Substitution substitution );

		protected final List< HigherTypeParameter > prepareTypeParameters(
				Substitution substitution
		) {
			Universe universe = declarationContext().universe();
			List< HigherTypeParameter > newTypeParams = new ArrayList<>();
			for( HigherTypeParameter t : typeParameters() ) {
				newTypeParams.add( new HigherTypeParameter(
						universe,
						t.identifier(),
						t.worldParameters.stream().map(
								x -> new World( universe, x.identifier() ) ).collect(
								Collectors.toList() )
				) );
			}
			for( int i = 0; i < typeParameters().size(); i++ ) {
				HigherTypeParameter oldTypeParam = typeParameters().get( i );
				HigherTypeParameter newTypeParam = newTypeParams.get( i );
				List< World > newWorldParams = newTypeParam.worldParameters;
//						oldTypeParam.worldParameters().stream()
//						.map( x -> new World( universe, x.identifier() ) ).collect(
//								Collectors.toList() );
				Substitution boundSubs = new Substitution() {
					@Override
					public World get( World placeHolder ) {
						int i = oldTypeParam.worldParameters.indexOf( placeHolder );
						return ( i == -1 )
								? substitution.get( placeHolder )
								: newWorldParams.get( i );
					}

					@Override
					public HigherReferenceType get( HigherTypeParameter placeHolder ) {
						int i = typeParameters().indexOf( placeHolder );
						return ( i == -1 )
								? substitution.get( placeHolder )
								: newTypeParams.get( i );
					}
				};
				oldTypeParam.innerType().upperBound().forEach(
						x -> newTypeParam.innerType().addUpperBound(
								x.applySubstitution( boundSubs ) ) );
				assert ( oldTypeParam.innerType().isBoundFinalised() );
				newTypeParam.innerType().finaliseBound();
			}
			return newTypeParams;
		}

		public boolean isSameSignature( HigherCallable other ) {
			if( !this.identifier().equals( other.identifier() )
					|| this.typeParameters.size() != other.typeParameters.size()
					|| this.arity() != other.arity() ) {
				return false;
			}
			Substitution s1 = new Substitution() {
				@Override
				public HigherReferenceType get( HigherTypeParameter placeHolder ) {
					int i = typeParameters.indexOf( placeHolder );
					return ( i == -1 )
							? placeHolder
							: other.typeParameters.get( i );
				}
			};
			Substitution s2 = new Substitution() {
				@Override
				public HigherReferenceType get( HigherTypeParameter placeHolder ) {
					int i = other.typeParameters().indexOf( placeHolder );
					return ( i == -1 )
							? placeHolder
							: typeParameters.get( i );
				}
			};
			for( int i = 0; i < this.typeParameters.size(); i++ ) {
				HigherTypeParameter t1 = this.typeParameters.get( i );
				HigherTypeParameter t2 = other.typeParameters.get( i );
				if( !t1.isSameKind( t2 ) ) {
					return false;
				}
				GroundReferenceType g1 = t1.applyTo( t2.worldParameters ).applySubstitution( s1 );
				GroundReferenceType g2 = t2.applyTo( t1.worldParameters ).applySubstitution( s2 );
				if( !t1.innerType().upperBound().allMatch( g2::isSubtypeOf )
						|| !t2.innerType().upperBound().allMatch( g1::isSubtypeOf ) ) {
					return false;
				}
			}
			for( int i = 0; i < this.arity(); i++ ) {
				GroundDataType t1 = this.innerCallable().signature().parameters().get( i ).type();
				GroundDataType t2 = other.innerCallable().signature().parameters().get( i ).type();
				if( !t1.isEquivalentTo( t2.applySubstitution( s2 ) ) ) {
					return false;
				}
			}
			return true;
		}

		private boolean isSameErasure( HigherCallable other ) {
			if( this.arity() != other.arity() || !this.identifier().equals( other.identifier() ) ) {
				return false;
			}
			return isSameSignature( other );
		}

		public boolean isSubSignature( HigherCallable other ) {
			return isSameSignature( other ) || isSameErasure( other );
		}

		public boolean isOverrideEquivalent( HigherCallable other ) {
			return this.isSubSignature( other ) || other.isSameErasure( this );
		}

		public boolean isProjectionOverloadEquivalent( HigherCallable other ) {
			return false; //ToDo
		}

		public void assertNoClash( HigherCallable other ) {

		}

		public int arity() {
			return innerCallable().signature().parameters().size();
		}

		public abstract Definition innerCallable();

		public abstract class Definition implements GroundCallable {

			Definition( Signature signature ) {
				this.signature = signature;
			}

			@Override
			public List< ? extends HigherReferenceType > typeArguments() {
				return typeParameters();
			}

			private final Signature signature;

			@Override
			public Signature signature() {
				return signature;
			}

			private boolean finalised = false;

			boolean isFinalised() {
				return finalised && signature().isFinalised();
			}

			public void finalise() {
				signature().finalise();
				finalised = true;
			}
		}

		protected abstract class Proxy implements GroundCallable {

			Proxy( Substitution substitution ) {
				this.substitution = substitution;
			}

			protected abstract Definition definition();

			private final Substitution substitution;

			protected final Substitution substitution() {
				return substitution;
			}

			public final List< ? extends HigherReferenceType > typeArguments() {
				return HigherCallable.this.typeParameters().stream().map(
						substitution()::get ).collect( Collectors.toList() );
			}

			public Signature signature() {
				return definition().signature().applySubstitution( substitution() );
			}
		}
	}

	public interface GroundCallable {
		List< ? extends HigherReferenceType > typeArguments();

		Signature signature();

		HigherCallable higherCallable();
	}

	public static final class HigherMethod extends HigherCallable {

		public HigherMethod(
				GroundClassOrInterface declarationContext,
				String identifier,
				EnumSet< Modifier > modifiers,
				List< HigherTypeParameter > typeParameters
		) {
			this( declarationContext, identifier, modifiers, typeParameters, new Signature(),
					true );
		}

		private HigherMethod(
				GroundClassOrInterface declarationContext,
				String identifier,
				EnumSet< Modifier > modifiers,
				List< HigherTypeParameter > typeParameters,
				Signature signature,
				boolean performChecks
		) {
			super( declarationContext, identifier, Variety.METHOD, modifiers, typeParameters,
					performChecks );
			assert ( !HigherCallable.CONSTRUCTOR_NAME.equals( identifier ) );
			this.innerCallable = new Definition( signature );
		}

		@Override
		public String toString() {
			return identifier() + innerCallable().signature();
		}

		public boolean isReturnTypeAssignable( HigherMethod other ) {
			if( this.innerCallable().returnType.isVoid() && other.innerCallable().returnType.isVoid() ) {
				return true;
			}
			if( !this.innerCallable().returnType.isVoid() && !other.innerCallable().returnType.isVoid() ) {
				Substitution s2 = new Substitution() {
					@Override
					public HigherReferenceType get( HigherTypeParameter placeHolder ) {
						int i = other.typeParameters().indexOf( placeHolder );
						return ( i == -1 )
								? placeHolder
								: HigherMethod.this.typeParameters().get( i );
					}
				};
				GroundDataType g2 = (GroundDataType) other.innerCallable.returnType.applySubstitution(
						s2 );
				return ( (GroundDataType) innerCallable().returnType ).isAssignableTo( g2 );
			}
			return false;
		}

		boolean selectionMethod = false;

		public boolean isSelectionMethod() {
			return selectionMethod;
		}

		public void setSelectionMethod() {
			this.selectionMethod = true;
		}

		private final HashMap< Substitution, HigherMethod > alphaIndex = new HashMap<>();

		@Override
		HigherMethod applySubstitution( Substitution substitution ) {
			HigherMethod result = alphaIndex.get( substitution );
			if( result == null ) {
				List< HigherTypeParameter > newTypeParams = prepareTypeParameters( substitution );
				Substitution newSubstitution = new Substitution() {

					@Override
					public World get( World placeHolder ) {
						return substitution.get( placeHolder );
					}

					@Override
					public HigherReferenceType get( HigherTypeParameter placeHolder ) {
						int i = typeParameters().indexOf( placeHolder );
						return ( i == -1 )
								? substitution.get( placeHolder )
								: newTypeParams.get( i );
					}
				};
				Signature signature = this.innerCallable.signature().applySubstitution(
						newSubstitution );
				result = new HigherMethod(
						this.declarationContext().applySubstitution( substitution ),
						identifier(),
						modifiers(),
						newTypeParams,
						signature,
						false
				);
				result.innerCallable.setReturnType(
						this.innerCallable.returnType.applySubstitution( newSubstitution ) );
				if( isSelectionMethod() ) {
					result.setSelectionMethod();
				}
				result.innerCallable.finalise();
				alphaIndex.put( substitution, result );
			}
			return result;
		}

		private final HashMap< List< ? extends HigherReferenceType >, GroundMethod > instIndex = new HashMap<>();

		@Override
		public GroundMethod applyTo( List< ? extends HigherReferenceType > typeArgs ) {
			GroundMethod result = instIndex.get( typeArgs );
			if( result == null ) {
				result = new Proxy( getApplicationSubstitution( typeArgs ) );
				instIndex.put( typeArgs, result );
			}
			return result;
		}

		private final Definition innerCallable;

		@Override
		public Definition innerCallable() {
			return innerCallable;
		}

		public final class Definition extends HigherCallable.Definition implements GroundMethod {

			private Definition( Signature signature ) {
				super( signature );
			}

			@Override
			public String toString() {
				return typeArguments().stream().map( HigherReferenceType::toString ).collect(
						Formatting.joining( ",", "<", ">", "" ) )
						+ identifier()
						+ signature();
			}

			private GroundDataTypeOrVoid returnType;

			public GroundDataTypeOrVoid returnType() {
				return returnType;
			}

			public void setReturnType( GroundDataTypeOrVoid type ) {
				assert ( !isFinalised() );
				this.returnType = type;
			}

			@Override
			public HigherMethod higherCallable() {
				return HigherMethod.this;
			}

		}

		public final class Proxy extends HigherCallable.Proxy implements GroundMethod {

			private Proxy( Substitution substitution ) {
				super( substitution );
			}

			@Override
			public String toString() {
				return typeArguments().stream().map( HigherReferenceType::toString ).collect(
						Formatting.joining( ",", "<", ">", "" ) )
						+ identifier()
						+ signature();
			}

			@Override
			protected Definition definition() {
				return HigherMethod.this.innerCallable();
			}

			public GroundDataTypeOrVoid returnType() {
				return definition().returnType().applySubstitution( substitution() );
			}

			@Override
			public HigherMethod higherCallable() {
				return HigherMethod.this;
			}

		}
	}


	public interface GroundMethod extends GroundCallable {

		GroundDataTypeOrVoid returnType();

		HigherMethod higherCallable();

	}

	public static final class HigherConstructor extends HigherCallable {

		public HigherConstructor(
				GroundClass declarationContext,
				EnumSet< Modifier > modifiers,
				List< HigherTypeParameter > typeParameters
		) {
			this( declarationContext, modifiers, typeParameters, new Signature(), true );
		}

		private HigherConstructor(
				GroundClass declarationContext,
				EnumSet< Modifier > modifiers,
				List< HigherTypeParameter > typeParameters,
				Signature signature,
				boolean performChecks
		) {
			super( declarationContext, HigherCallable.CONSTRUCTOR_NAME, Variety.CONSTRUCTOR,
					modifiers, typeParameters, performChecks );
			this.innerCallable = new Definition( signature );
		}

		@Override
		public String toString() {
			return declarationContext().typeConstructor().identifier() + innerCallable().signature();
		}

		public GroundClass declarationContext() {
			return (GroundClass) super.declarationContext();
		}

		private final HashMap< Substitution, HigherConstructor > alphaIndex = new HashMap<>();

		@Override
		HigherConstructor applySubstitution( Substitution substitution ) {
			HigherConstructor result = alphaIndex.get( substitution );
			if( result == null ) {
				List< HigherTypeParameter > newTypeParams = prepareTypeParameters( substitution );
				Substitution newSubstitution = new Substitution() {

					@Override
					public World get( World placeHolder ) {
						return substitution.get( placeHolder );
					}

					@Override
					public HigherReferenceType get( HigherTypeParameter placeHolder ) {
						int i = typeParameters().indexOf( placeHolder );
						return ( i == -1 )
								? substitution.get( placeHolder )
								: newTypeParams.get( i );
					}
				};
				result = new HigherConstructor(
						this.declarationContext().applySubstitution( substitution ),
						modifiers(),
						newTypeParams,
						this.innerCallable.signature().applySubstitution( newSubstitution ),
						false
				);
				result.innerCallable.finalise();
				alphaIndex.put( substitution, result );
			}
			return result;
		}

		private final HashMap< List< ? extends HigherReferenceType >, GroundConstructor > instIndex = new HashMap<>();

		@Override
		public GroundConstructor applyTo( List< ? extends HigherReferenceType > typeArgs ) {
			GroundConstructor result = instIndex.get( typeArgs );
			if( result == null ) {
				result = new Proxy( getApplicationSubstitution( typeArgs ) );
				instIndex.put( typeArgs, result );
			}
			return result;
		}

		private final Definition innerCallable;

		@Override
		public Definition innerCallable() {
			return innerCallable;
		}

		public final class Definition extends HigherCallable.Definition
				implements GroundConstructor {

			private Definition( Signature signature ) {
				super( signature );
			}

			@Override
			public String toString() {
				return typeArguments().stream().map( HigherReferenceType::toString ).collect(
						Formatting.joining( ",", "<", ">", "" ) )
						+ declarationContext().typeConstructor().identifier()
						+ signature();
			}

			@Override
			public HigherConstructor higherCallable() {
				return HigherConstructor.this;
			}

		}

		public final class Proxy extends HigherCallable.Proxy implements GroundConstructor {

			private Proxy( Substitution substitution ) {
				super( substitution );
			}

			@Override
			public String toString() {
				return typeArguments().stream().map( HigherReferenceType::toString ).collect(
						Formatting.joining( ",", "<", ">", "" ) )
						+ declarationContext().typeConstructor().identifier()
						+ signature();
			}

			@Override
			protected Definition definition() {
				return HigherConstructor.this.innerCallable();
			}

			@Override
			public HigherConstructor higherCallable() {
				return HigherConstructor.this;
			}

		}
	}

	public interface GroundConstructor extends GroundCallable {
		HigherConstructor higherCallable();
	}
}
