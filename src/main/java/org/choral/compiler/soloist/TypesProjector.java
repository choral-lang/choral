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

package org.choral.compiler.soloist;

import org.choral.ast.Name;
import org.choral.ast.Node;
import org.choral.ast.body.FormalMethodParameter;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.type.WorldArgument;
import org.choral.ast.visitors.AbstractSoloistProjector;
import org.choral.ast.visitors.PrettyPrinterVisitor;
import org.choral.compiler.unitNormaliser.UnitRepresentation;
import org.choral.exceptions.ChoralException;
import org.choral.types.DataType;
import org.choral.types.GroundDataType;
import org.choral.types.HigherDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class TypesProjector extends AbstractSoloistProjector< List< ? extends Node > > {

	private TypesProjector( WorldArgument world ) {
		super( world );
	}

	public static < T extends Node > List< T > visit( WorldArgument w, T n ) {
		return new TypesProjector( w ).safeVisit( n );
	}

	static < T extends Node > List< T > visitAndCollect( WorldArgument w, List< T > n ) {
		return new TypesProjector( w ).visitAndCollect( n );
	}

	@Override
	public List< TypeExpression > visit( TypeExpression n ) {
		if( ( n.worldArguments().size() == 0 ) // it is a @ => ...
				|| n.worldArguments().contains( this.world() ) // it is a *
		) {
			if( n.typeAnnotation().isEmpty() && n.name().identifier().equals( "void" ) ){
				return Collections.singletonList( n );
			}
			DataType dataType = n.typeAnnotation().get();
			if( dataType.isHigherType() ){
				HigherDataType higherDataType = ( HigherDataType ) dataType;
				return higherDataType.worldParameters().stream()
						.map( w ->
							new TypeExpression(
									new Name( Utils.getProjectionName(
											n.name().identifier(),
											new WorldArgument( new Name( w.identifier() ) ),
											higherDataType.worldParameters().stream()
													.map( wp -> new WorldArgument( new Name( wp.identifier() ) ) )
													.collect( Collectors.toList() )
									) ),
									Collections.singletonList( this.world ),
									n.typeArguments().stream()
									.map( this::visit )
									.flatMap( List::stream )
									.collect( Collectors.toList() )
							).< TypeExpression >copyPosition( n )
						)
						.collect( Collectors.toList() );
			} else {
				GroundDataType groundDataType = ( GroundDataType ) dataType;
				return Collections.singletonList(
						new TypeExpression(
								new Name( Utils.getProjectionName(
										n.name().identifier(),
										this.world(),
										n.worldArguments(),
										groundDataType.typeConstructor().worldParameters().stream()
												.map( w -> new WorldArgument( new Name ( w.identifier() ) ) )
												.collect( Collectors.toList() )
								) ),
								Collections.singletonList( this.world() ),
								n.typeArguments().stream()
								.map( this::visit )
								.flatMap( List::stream )
								.collect( Collectors.toList() )
						).copyPosition( n )
				);
			}
		} else {
			return singletonList( UnitRepresentation.getType( this.world() ) );
		}
	}

	@Override
	public List< WorldArgument > visit( WorldArgument n ) {
		throw new ChoralException( "The soloist projection should not be launched on worlds" );
	}

	@Override
	public List< FormalTypeParameter > visit( FormalTypeParameter n ) {
		return n.worldParameters().stream().map(
				w -> new FormalTypeParameter(
						new Name( Utils.getProjectionName(
								n.name().identifier(),
								w.toWorldArgument(),
								n.worldParameters().stream()
										.map( FormalWorldParameter::toWorldArgument )
										.collect( Collectors.toList() ) ) ),
						emptyList(),
						visitAndCollect( w.toWorldArgument(), n.upperBound() )
				).< FormalTypeParameter >copyPosition( n )
		).collect( Collectors.toList() );
	}

	@Override
	public List< FormalMethodParameter > visit( FormalMethodParameter n ) {
		if( n.type().worldArguments().contains( this.world() ) ) {
			return Collections.singletonList(
					new FormalMethodParameter(
							n.name(),
							visit( world(), n.type() ).get( 0 ),
							n.position() )
			);
		} else {
			return emptyList();
		}
	}

	private < T extends Node > List< T > visitAndCollect( List< T > n ) {
		return n.stream().flatMap( a -> safeVisit( a ).stream() ).collect( Collectors.toList() );
	}

	@SuppressWarnings( "unchecked cast" )
	private < T extends Node > List< T > safeVisit( T n ) {
		return (List< T >) n.accept( this );
	}

}
