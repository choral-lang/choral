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

package org.choral.compiler.unitNormaliser;

import org.choral.ast.Name;
import org.choral.ast.expression.*;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.type.WorldArgument;

import java.util.Collections;
import java.util.List;

public final class UnitRepresentation {

	public static final Name UNIT = new Name( "Unit" );
	public static final Name UID = new Name( "id" );

	public static ScopedExpression UnitFD( WorldArgument world ) {
		return new ScopedExpression(
				new StaticAccessExpression( new TypeExpression(
						UNIT,
						Collections.singletonList( world ),
						Collections.emptyList()
				) ), new FieldAccessExpression( UID ) );
	}

	public static Expression unitMC( List< Expression > a, WorldArgument world ) {
		return new ScopedExpression(
				new StaticAccessExpression( new TypeExpression(
						UNIT,
						Collections.singletonList( world ),
						Collections.emptyList()
				) ),
				new MethodCallExpression( UnitRepresentation.UID, a, Collections.emptyList() ) );
	}

	public static TypeExpression getType( WorldArgument world ) {
		return new TypeExpression( UNIT, Collections.singletonList( world ),
				Collections.emptyList() );
	}

}
