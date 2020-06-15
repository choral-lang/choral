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

package org.choral.types.kinds;

import java.util.Objects;

public class Arrow extends Kind {

	private final Kind domain;
	private final Kind codomain;

	Arrow( Kind domain, Kind codomain ) {
		assert domain != null;
		assert codomain != null;
		this.domain = domain;
		this.codomain = codomain;
	}

	public Kind domain() {
		return domain;
	}

	public Kind codomain() {
		return codomain;
	}

	@Override
	public Kind apply( Kind argument ) {
		if( argument == this.domain ) {
			return this.codomain;
		} else {
			throw new KindApplicationException( this, argument );
		}
	}

	@Override
	protected boolean isArrow() {
		return true;
	}

	@Override
	public String toString() {
		if( domain.isArrow() ) {
			return "(" + domain + ") => " + codomain;
		} else {
			return domain + " => " + codomain;
		}
	}

	@Override
	public boolean equals( Object o ) {
		if( o == this ) {
			return true;
		} else if( o instanceof Arrow ) {
			Arrow a = (Arrow) o;
			return this.domain.equals( a.domain ) && this.codomain.equals( a.codomain );
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash( domain, codomain );
	}
}
