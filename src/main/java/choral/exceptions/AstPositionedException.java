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

public class AstPositionedException extends ChoralException {
	protected ChoralException innerException;
	protected String innerMessage;
	private Position position;

	public AstPositionedException( Position position, ChoralException innerException ) {
		super( innerException );
		this.innerMessage = innerException.getMessage();
		this.innerException = innerException;
		this.position = position; //new Position[]{position} ;
	}

	public AstPositionedException( Node node, ChoralException innerException ) {
		this( node.position(), innerException );
	}

//    public AstPositionedException(Position[] positions, ChoralException innerException) {
//        super(innerException);
//        this.innerMessage = innerException.getMessage();
//        this.innerException = innerException;
//        this.positions = positions ;
//    }

//    protected AstPositionedException(Position position, String message) {
//        super(message + "\n" + position.formattedPosition() );
//        this.innerMessage = message;
//        this.position = new Position[]{ position } ;
//    }

	public String getInnerMessage() {
		return this.innerMessage;
	}

	public Position position() {
		return position;
	}

}
