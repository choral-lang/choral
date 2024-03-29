/*
 * Copyright (C) 2019-2020 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 * Copyright (C) 2019-2020 by Fabrizio Montesi <famontesi@gmail.com>
 * Copyright (C) 2019-2020 by Marco Peressotti <marco.peressotti@gmail.com>
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

package choral.ast.body;

import choral.ast.Node;
import choral.ast.Position;
import choral.types.Member;

import java.util.List;
import java.util.Optional;

public abstract class MethodDefinition extends Node {
	private final MethodSignature signature;

	private final List< Annotation > annotations;

	public MethodDefinition(
			final MethodSignature signature,
			final List< Annotation > annotations,
			Position position
	) {
		super( position );
		this.signature = signature;
		this.annotations = annotations;
	}

	private Member.HigherMethod typeAnnotation;

	public Optional< ? extends Member.HigherMethod > typeAnnotation() {
		return Optional.ofNullable( typeAnnotation );
	}

	public void setTypeAnnotation( Member.HigherMethod typeAnnotation ) {
		this.typeAnnotation = typeAnnotation;
		this.signature.setTypeAnnotation( typeAnnotation );
	}

	public MethodSignature signature() {
		return signature;
	}

	public List< Annotation > annotations() {
		return annotations;
	}
}
