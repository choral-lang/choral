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

package choral.ast.body;

import choral.ast.Name;
import choral.ast.Position;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.FormalWorldParameter;
import choral.types.HigherClass;
import choral.types.HigherClassOrInterface;

import java.util.List;
import java.util.Optional;

import static choral.ast.body.ClassModifier.*;

public abstract class TemplateDeclaration extends RefType {

	protected TemplateDeclaration(
			Name name, List< FormalWorldParameter > worldparameters,
			List< FormalTypeParameter > typeparameters, Position position
	) {
		super( name, worldparameters, typeparameters, position );
	}

	public abstract Optional< ? extends HigherClassOrInterface > typeAnnotation();

	public abstract boolean isPublic();

	public abstract boolean isProtected();

	public abstract boolean isPrivate();

	public abstract boolean isFinal();

	public abstract boolean isAbstract();

	public final boolean isPackagePrivate() {
		return !( isPrivate() || isProtected() || isPublic() );
	}
}
