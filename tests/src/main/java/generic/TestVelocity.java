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

package generic;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import choral.ast.expression.FieldAccessExpression;
import choral.ast.Name;

import java.io.StringReader;
import java.io.StringWriter;

public class TestVelocity {

	public static void main( String[] args ) throws ParseException {
		FieldAccessExpression e = new FieldAccessExpression( new Name( "myMethod" ) );

		VelocityContext vc = new VelocityContext();
		vc.put( "name", e );
		vc.put( "qualifiers", e );

		StringWriter sw = new StringWriter();
		String template = "Username is $f";
		getVelocityTemplate( template ).merge( vc, sw );

		System.out.println( sw.toString() );
	}

	public static Template getVelocityTemplate( String template ) throws ParseException {
		RuntimeServices rs = RuntimeSingleton.getRuntimeServices();

		StringReader sr = new StringReader( template );
		SimpleNode sn = rs.parse( sr, "" );

		Template t = new Template();
		t.setRuntimeServices( rs );
		t.setData( sn );
		t.initDocument();

		return t;
	}

}
