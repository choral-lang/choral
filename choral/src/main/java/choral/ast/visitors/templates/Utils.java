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

package choral.ast.visitors.templates;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

public class Utils {

	private Utils() {
	}

	public class _Template {
		Template t;

		private _Template( Template t ) {
			this.t = t;
		}

		public String render( HashMap< String, Object > m ) {
			VelocityContext vc = new VelocityContext( m );
			StringWriter sw = new StringWriter();
			t.merge( vc, sw );
			return sw.toString();
		}

	}

	private static _Template getTemplate( Template t ) {
		return new Utils().new _Template( t );
	}

	public static _Template createVelocityTemplate( String template ) {
		RuntimeServices rs = RuntimeSingleton.getRuntimeServices();
		StringReader sr = new StringReader( template );
		SimpleNode sn = null;
		try {
			sn = rs.parse( sr, "" );
		} catch( ParseException e ) {
			e.printStackTrace();
		}

		Template t = new Template();
		t.setRuntimeServices( rs );
		t.setData( sn );
		t.initDocument();

		return getTemplate( t );
	}
}
