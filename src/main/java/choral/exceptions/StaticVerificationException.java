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

package choral.exceptions;

import choral.ast.Node;
import choral.ast.Position;
import choral.types.Type;

import java.util.Optional;

public class StaticVerificationException extends ChoralException {
	public StaticVerificationException( String message ) {
		super( message );
	}

	public static ChoralException of( String message, Optional< ? extends Node > p ) {
		StaticVerificationException e = new StaticVerificationException( message );
		if( p.isPresent() ) {
			return new AstPositionedException( p.get(), e );
		} else {
			return e;
		}
	}

	public static ChoralException of( String message, Position p ) {
		StaticVerificationException e = new StaticVerificationException( message );
		return new AstPositionedException( p, e );
	}

}
