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

import java.util.Iterator;
import java.util.stream.Stream;

public class Sensor {

	public Sensor () {}

	private final Signature validSignature = new Signature( "1fffd0e1-e60c-4364-8b4a-94f14dcc956e" );
	private final Signature invalidSignature = new Signature( "0fffd0e1-e60c-4364-8b4a-94f14dcc956e" );

	private final Iterator< VitalsMsg > i = Stream.of(
			new VitalsMsg( validSignature, new Vitals( "John Doe", "65", "36.5", "1" ) ),
			new VitalsMsg( validSignature, new Vitals( "John Doe", "68", "36.5", "3" ) ),
			new VitalsMsg( validSignature, new Vitals( "John Doe", "69", "36.6", "3" ) ),
			new VitalsMsg( validSignature, new Vitals( "John Doe", "70", "36.6", "4" ) ),
			new VitalsMsg( invalidSignature, new Vitals( "John Doe", "100", "38.5", "15" ) ),
			new VitalsMsg( validSignature, new Vitals( "John Doe", "72", "36.6", "4" ) ),
			new VitalsMsg( validSignature, new Vitals( "John Doe", "73", "36.7", "4" ) ),
			new VitalsMsg( invalidSignature, new Vitals( "John Doe", "130", "38.6", "17" ) ),
			new VitalsMsg( validSignature, new Vitals( "John Doe", "72", "36.7", "3" ) ),
			new VitalsMsg( validSignature, new Vitals( "John Doe", "68", "36.7", "2" ) ),
			new VitalsMsg( validSignature, new Vitals( "John Doe", "65", "36.6", "2" ) ),
			new VitalsMsg( validSignature, new Vitals( "John Doe", "64", "36.5", "1" ) ),
			new VitalsMsg( invalidSignature, new Vitals( "John Doe", "150", "38.8", "20" ) )
	).iterator();

	public boolean isOn () {
		return i.hasNext();
	}

	public VitalsMsg next () {
		return i.next();
	}

}
