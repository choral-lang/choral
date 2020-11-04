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

package choral.examples.VitalsStreamingUtils;

import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
public class Vitals {

	private final String id;
	private final String heartRate;
	private final String temperature;
	private final String motion;

	public Vitals ( String id, String heartRate, String temperature, String motion ) {
		this.id = id;
		this.heartRate = heartRate;
		this.temperature = temperature;
		this.motion = motion;
	}

	public String id () {
		return id;
	}

	public String heartRate () {
		return heartRate;
	}

	public String temperature () {
		return temperature;
	}

	public String motion () {
		return motion;
	}
}
