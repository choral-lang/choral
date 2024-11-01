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
import choral.types.kinds.Kind;
import choral.utils.Formatting;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static choral.types.Modifier.ABSTRACT;
import static choral.types.Modifier.PUBLIC;

public class Universe {

	public Universe() {
		for( PrimitiveTypeTag tag : PrimitiveTypeTag.values() ) {
			primitiveTypes.put( tag, new HigherPrimitiveDataType( this, tag ) );
		}
	}

	private final Package rootPackage = Package.createNewRoot( this );

	public Package rootPackage() {
		return rootPackage;
	}

	private static final HashMap< String, SpecialTypeTag > specialClassesConversionMap = new HashMap<>(
			11 );

	public enum SpecialTypeTag {
		OBJECT( "java.lang.Object", HigherClassOrInterface.Variety.CLASS ),
		ENUM( "java.lang.Enum", HigherClassOrInterface.Variety.CLASS ),
		STRING( "java.lang.String", HigherClassOrInterface.Variety.CLASS ),
		EXCEPTION( "java.lang.Exception", HigherClassOrInterface.Variety.CLASS ),
		NUMBER( "java.lang.Number", HigherClassOrInterface.Variety.CLASS ),
		BOOLEAN( "java.lang.Boolean", HigherClassOrInterface.Variety.CLASS ),
		BYTE( "java.lang.Byte", HigherClassOrInterface.Variety.CLASS ),
		CHARACTER( "java.lang.Character", HigherClassOrInterface.Variety.CLASS ),
		SHORT( "java.lang.Short", HigherClassOrInterface.Variety.CLASS ),
		INTEGER( "java.lang.Integer", HigherClassOrInterface.Variety.CLASS ),
		LONG( "java.lang.Long", HigherClassOrInterface.Variety.CLASS ),
		FLOAT( "java.lang.Float", HigherClassOrInterface.Variety.CLASS ),
		DOUBLE( "java.lang.Double", HigherClassOrInterface.Variety.CLASS );

		final String qualifiedName;
		final HigherClassOrInterface.Variety variety;

		SpecialTypeTag( String qualifiedName, HigherClassOrInterface.Variety variety ) {
			this.qualifiedName = qualifiedName;
			this.variety = variety;
			specialClassesConversionMap.put( qualifiedName, this );
		}

		@Override
		public String toString() {
			return qualifiedName;
		}
	}

	private static final HashMap< String, PrimitiveTypeTag > primitiveTypesConversionMap = new HashMap<>(
			8 );
	private static final Map< SpecialTypeTag, PrimitiveTypeTag > unboxingMap = new EnumMap<>(
			SpecialTypeTag.class );

	public enum PrimitiveTypeTag {
		BOOLEAN( "boolean", SpecialTypeTag.BOOLEAN ),
		BYTE( "byte", SpecialTypeTag.BYTE ),
		CHAR( "char", SpecialTypeTag.CHARACTER ),
		SHORT( "short", SpecialTypeTag.SHORT ),
		INT( "int", SpecialTypeTag.INTEGER ),
		LONG( "long", SpecialTypeTag.LONG ),
		FLOAT( "float", SpecialTypeTag.FLOAT ),
		DOUBLE( "double", SpecialTypeTag.DOUBLE );

		final String identifier;
		final SpecialTypeTag boxedType;

		public SpecialTypeTag boxedType() {
			return boxedType;
		}

		public boolean isNumeric() {
			return this != BOOLEAN;
		}

		public boolean isIntegral() {
			return switch( this ) {
				case CHAR, BYTE, SHORT, INT, LONG -> true;
				default -> false;
			};
		}

		public boolean isAssignableTo( PrimitiveTypeTag target ) {
			if( target == null ) {
				return false;
			}
			return switch( this ) {
				case BOOLEAN -> target == BOOLEAN;
				case BYTE -> target != CHAR && this.compareTo( target ) <= 0;
				default -> this.compareTo( target ) <= 0;
			};
		}

		PrimitiveTypeTag( String identifier, SpecialTypeTag boxedType ) {
			this.identifier = identifier;
			this.boxedType = boxedType;
			primitiveTypesConversionMap.put( identifier, this );
			unboxingMap.put( boxedType, this );
		}

		@Override
		public String toString() {
			return identifier;
		}

	}

	private final Map< PrimitiveTypeTag, HigherPrimitiveDataType > primitiveTypes = new EnumMap<>(
			PrimitiveTypeTag.class );

	public HigherPrimitiveDataType primitiveDataType( PrimitiveTypeTag key ) {
		return primitiveTypes.get( key );
	}

	public Optional< ? extends HigherPrimitiveDataType > primitiveDataType( String identifier ) {
		PrimitiveTypeTag key = primitiveTypesConversionMap.get( identifier );
		if( key == null ) {
			return Optional.empty();
		} else {
			return Optional.of( primitiveTypes.get( key ) );
		}
	}

	public boolean isBoxedType( HigherClass type ) {
		return unboxingMap.containsKey( type.specialTypeTag() );
	}

	public HigherPrimitiveDataType unboxedType( HigherClass type ) {
		return primitiveDataType( unboxingMap.get( type.specialTypeTag() ) );
	}

	private final Map< SpecialTypeTag, HigherClassOrInterface > specialClasses = new EnumMap<>(
			SpecialTypeTag.class );

	SpecialTypeTag registerSpecialType( HigherClassOrInterface type ) {
		assert ( type != null );
		SpecialTypeTag key = specialClassesConversionMap.get( type.identifier( true ) );
		if( key != null ) {
			if( key.variety == type.variety() ) {
				specialClasses.put( key, type );
			} else {
				throw new StaticVerificationException(
						"Invalid special type '" + type.identifier() + "', expected " + key.variety.labelSingular + " found " + type.variety() );
			}
		}
		return key;
	}

	public HigherClassOrInterface specialType( SpecialTypeTag key ) {
		HigherClassOrInterface result = specialClasses.get( key );
		if( result == null ) {
			throw new StaticVerificationException( "Unknown class '"
					+ key.qualifiedName
					+ "', missing a header?" );
		}
		return result;
	}

	Map< Integer, HigherClass > topClassesIndex = new HashMap<>( 10 );

	HigherClass topReferenceType( int worlds ) {
		assert ( worlds > 0 );
		HigherClass t;
		if( worlds == 1 ) {
			t = (HigherClass) this.specialType( Universe.SpecialTypeTag.OBJECT );
		} else {
			t = topClassesIndex.get( worlds );
			if( t == null ) {
				List< World > ws = new ArrayList<>( worlds );
				for( int i = 0; i < worlds; i++ ) {
					ws.add( new World( this, "A" + i ) );
				}
				t = new HigherClass( rootPackage(),
						EnumSet.of( PUBLIC, ABSTRACT ),
						"Any",
						ws, List.of(),
						false );
				t.innerType().finaliseInheritance();
				t.innerType().finaliseInterface();
				topClassesIndex.put( worlds, t );
			}
		}
		return t;
	}

	GroundClass topReferenceType( List< ? extends World > worlds ) {
		return topReferenceType( worlds.size() ).applyTo( worlds );
	}

	Map< Integer, HigherReferenceType > nullTypesIndex = new HashMap<>( 10 );

	public HigherReferenceType nullType( int worlds ) {
		assert ( worlds > 0 );
		HigherReferenceType t = nullTypesIndex.get( worlds );
		if( t == null ) {
			List< World > ws = new ArrayList<>( worlds );
			for( int i = 0; i < worlds; i++ ) {
				ws.add( new World( this, "A" + i ) );
			}
			t = new HigherNullType( ws );
			nullTypesIndex.put( worlds, t );
		}
		return t;
	}

	public GroundReferenceType nullType( List< ? extends World > worlds ) {
		return nullType( worlds.size() ).applyTo( worlds );
	}

	private final class HigherNullType extends HigherReferenceType {

		HigherNullType( List< World > worldParameters ) {
			super( Universe.this, worldParameters );
		}

		public String toString() {
			return this.worldParameters.stream().map( World::toString ).collect(
					Formatting.joining( ",", "@(", ")->(", "" ) )
					+ "Null"
					+ this.worldParameters.stream().map( World::toString ).collect(
					Formatting.joining( ",", "@(", ")", "" ) )
					+ ")";
		}

		private final Map< List< ? extends World >, GroundNullType > alphaIndex = new HashMap<>();

		@Override
		public GroundNullType applyTo( List< ? extends World > args ) {
			GroundNullType result = alphaIndex.get( args );
			if( result == null ) {
				result = new GroundNullType( args );
				alphaIndex.put( args, result );
			}
			return result;
		}

		final class GroundNullType extends TypeBase implements GroundReferenceType {

			public GroundNullType( List< ? extends World > worldArguments ) {
				super( Universe.this );
				this.worldArguments = worldArguments;
			}

			private final List< ? extends World > worldArguments;

			@Override
			public List< ? extends World > worldArguments() {
				return worldArguments;
			}

			public String toString() {
				return "Null" +
						worldArguments().stream().map( World::toString ).collect(
								Formatting.joining( ",", "@(", ")", "" ) );
			}

			@Override
			public HigherNullType typeConstructor() {
				return HigherNullType.this;
			}

			@Override
			public GroundNullType applySubstitution( Substitution substitution ) {
				return typeConstructor().applyTo( worldArguments().stream()
						.map( substitution::get ).collect( Collectors.toList() ) );
			}

			@Override
			public Stream< ? extends Member.Field > fields() {
				return Stream.of();
			}

			@Override
			public Optional< ? extends Member.Field > field( String name ) {
				return Optional.empty();
			}

			@Override
			public Stream< ? extends Member.HigherMethod > methods() {
				return Stream.of();
			}

			@Override
			public Stream< ? extends Member.HigherMethod > methods( String name ) {
				return Stream.of();
			}

			@Override
			public boolean isInterfaceFinalised() {
				return true;
			}

			@Override
			public boolean isInstantiationChecked() {
				return true;
			}

			@Override
			public void checkInstantiation() {

			}

			@Override
			public Kind kind() {
				return Kind.getStar();
			}

			@Override
			public boolean isEquivalentTo( Type type ) {
				if( type instanceof GroundNullType ) {
					GroundNullType t = (GroundNullType) type;
					return worldArguments.equals( t.worldArguments );
				}
				return false;
			}

			@Override
			public boolean isSubtypeOf( Type type, boolean strict ) {
				if( strict && type instanceof GroundNullType ) {
					return false;
				}
				if( type instanceof GroundReferenceType ) {
					GroundReferenceType t = (GroundReferenceType) type;
					return worldArguments.equals( t.worldArguments() );
				}
				return false;
			}

			@Override
			public boolean isSubtypeOf_relaxed( Type type, boolean strict ) {
				if( strict && type instanceof GroundNullType ) {
					return false;
				}
				if( type instanceof GroundReferenceType ) {
					GroundReferenceType t = (GroundReferenceType) type;
					return true;
				}
				return false;
			}

			@Override
			public boolean isEquivalentToErasureOf( GroundDataType type ) {
				return isEquivalentTo( type );
			}
		}

	}

	private final GroundDataTypeOrVoid voidType = new GroundDataTypeOrVoid() {
		@Override
		public GroundDataTypeOrVoid applySubstitution( Substitution substitution ) {
			return this;
		}

		@Override
		public boolean isAssignableTo( GroundDataTypeOrVoid type ) {
			return false;
		}

		@Override
		public SpecialTypeTag specialTypeTag() {
			return null;
		}

		@Override
		public PrimitiveTypeTag primitiveTypeTag() {
			return null;
		}

		@Override
		public boolean isVoid() {
			return true;
		}

		@Override
		public boolean isPrimitive() {
			return false;
		}

		@Override
		public boolean isTypeParameter() {
			return false;
		}

		@Override
		public boolean isClass() {
			return false;
		}

		@Override
		public boolean isInterface() {
			return false;
		}

		@Override
		public boolean isEnum() {
			return false;
		}

		@Override
		public boolean isHigherType() {
			return false;
		}

		@Override
		public String toString() {
			return "void";
		}
	};

	public GroundDataTypeOrVoid voidType() {
		return voidType;
	}

//	private final class HigherVoidType extends HigherDataType {
//
//		HigherVoidType( List< World > worldParameters ) {
//			super( Types.this, worldParameters );
//		}
//
//		public String toString() {
//			return this.worldParameters.stream().map( World::toString ).collect(
//					Formatting.joining( ",", "@(", ")->(", "" ) )
//					+ "void"
//					+ this.worldParameters.stream().map( World::toString ).collect(
//					Formatting.joining( ",", "@(", ")", "" ) )
//					+ ")";
//		}
//
//		private final Map< List< ? extends World >, GroundVoidType > alphaIndex = new HashMap<>();
//
//		@Override
//		public GroundVoidType applyTo( List< ? extends World > args ) {
//			GroundVoidType result = alphaIndex.get( args );
//			if( result == null ) {
//				result = new GroundVoidType( args );
//				alphaIndex.put( args,result );
//			}
//			return result;
//		}
//
//		final class GroundVoidType extends TypeBase implements GroundDataType {
//
//			public GroundVoidType( List< ? extends World > worldArguments ) {
//				super( Types.this );
//				this.worldArguments = worldArguments;
//			}
//
//			private final List< ? extends World > worldArguments;
//
//			@Override
//			public List< ? extends World > worldArguments() {
//				return worldArguments;
//			}
//
//			public String toString() {
//				return "void" +
//						worldArguments().stream().map( World::toString ).collect(
//								Formatting.joining( ",", "@(", ")", "" ) );
//			}
//
//			@Override
//			public HigherVoidType typeConstructor() {
//				return HigherVoidType.this;
//			}
//
//			@Override
//			public GroundVoidType applySubstitution( Substitution substitution ) {
//				return typeConstructor().applyTo( worldArguments().stream()
//						.map( substitution::get ).collect( Collectors.toList() ) );
//			}
//
//			@Override
//			public boolean isInstantiationChecked() {
//				return true;
//			}
//
//			@Override
//			public void checkInstantiation() {
//
//			}
//
//			@Override
//			public Kind kind() {
//				return Kind.getStar();
//			}
//
//			@Override
//			public boolean isEquivalentTo( Type type ) {
//				if( type instanceof GroundVoidType ) {
//					GroundVoidType t = (GroundVoidType) type;
//					return worldArguments.equals( t.worldArguments );
//				}
//				return false;
//			}
//
//			@Override
//			public boolean isSubtypeOf( Type type, boolean strict ) {
//				if( strict && type instanceof GroundVoidType ) {
//					return false;
//				}
//				if( type instanceof GroundReferenceType ) {
//					GroundReferenceType t = (GroundReferenceType) type;
//					return worldArguments.equals( t.worldArguments() );
//				}
//				return false;
//			}
//		}
//
//	}

}
