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

import choral.ast.Node;
import choral.ast.Position;
import choral.ast.WithTypeAnnotation;
import choral.ast.visitors.MergerInterface;
import choral.types.GroundDataTypeOrVoid;

import java.util.Optional;

public abstract class Expression extends Node
		implements WithTypeAnnotation< GroundDataTypeOrVoid > {

	protected Expression() {
		super();
	}

	protected Expression( Position position ) {
		super( position );
	}

//	@Override
//	public Optional<? extends GroundDataType> typeAnnotation() {
//		;
//	}

	private GroundDataTypeOrVoid typeAnnotation;

	public Optional< ? extends GroundDataTypeOrVoid > typeAnnotation() {
		return Optional.ofNullable( typeAnnotation );
	}

	public void setTypeAnnotation( GroundDataTypeOrVoid typeAnnotation ) {
		this.typeAnnotation = typeAnnotation;
	}

	public abstract < R, T extends Node > R merge( MergerInterface< R > m, T n );

}
