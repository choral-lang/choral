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

package org.choral.compiler.merge;

import org.choral.ast.expression.*;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.visitors.AbstractMerger;
import org.choral.ast.visitors.PrettyPrinterVisitor;
import org.choral.exceptions.ChoralException;

import java.util.ArrayList;
import java.util.List;

public class ExpressionsMerger extends AbstractMerger< Expression > {

	private ExpressionsMerger() {
		super();
	}

	final private PrettyPrinterVisitor ppv = new PrettyPrinterVisitor();

	static Expression mergeExpressions( Expression n1, Expression n2 ) {
		return new ExpressionsMerger().merge( n1, n2 );
	}

	@Override
	public Expression merge( Expression n1, Expression n2 ) {
		try {
			return n1.merge( this, n2 );
		} catch( ChoralException e ) {
			throw new MergeException( e.getMessage(), n1, n2 );
		}
	}

	@Override
	public Expression merge( AssignExpression n1, AssignExpression n2 ) {
		MergeException._assert(
				n1.operator().equals( n2.operator() ),
				"Cannot merge assignment due to different operators: " +
						n1.operator() + " and " + n2.operator(), n1, n2
		);
		return new AssignExpression(
				merge( n1.value(), n2.value() ),
				merge( n1.target(), n2.target() ),
				n1.operator()
		);
	}

	@Override
	public Expression merge( BinaryExpression n1, BinaryExpression n2 ) {
		MergeException._assert(
				n1.operator().equals( n2.operator() ),
				"Cannot merge binary expression due to different operators: " +
						n1.operator() + " and " + n2.operator(), n1, n2
		);
		return new BinaryExpression(
				merge( n1.left(), n2.left() ),
				merge( n1.right(), n2.right() ),
				n1.operator()
		);
	}

	@Override
	public Expression merge( ClassInstantiationExpression n1, ClassInstantiationExpression n2 ) {
		String errorPrefix = "Cannot merge class instantiation expression due to ";
		MergeException._assert(
				n1.typeExpression().name().equals( n2.typeExpression().name() ),
				errorPrefix + "different class names: " + n1.typeExpression().name() + " and " + n2.typeExpression().name(),
				n1, n2
		);
		MergeException._assert(
				n1.typeArguments().size() == n2.typeArguments().size(),
				errorPrefix + "different generic argument sizes: " + n1.typeArguments().size() + " and " + n2.typeArguments().size(),
				n1, n2
		);
		for( int i = 0; i < n1.typeArguments().size(); i++ ) {
			MergeException._assert(
					n1.typeArguments().get( i ).equals( n2.typeArguments().get( i ) ),
					errorPrefix + "different class types: " + ppv.visit(
							n1.typeArguments().get( i ) ) + " and " + ppv.visit(
							n2.typeArguments().get( i ) ), n1, n2
			);
		}
		MergeException._assert(
				n1.arguments().size() == n2.arguments().size(),
				errorPrefix + "different parameter sizes: " + n1.arguments().size() + " and " + n2.arguments().size(),
				n1, n2
		);
		List< Expression > arguments = new ArrayList<>();
		for( int i = 0; i < n1.arguments().size(); i++ ) {
			arguments.add( merge( n1.arguments().get( i ), n2.arguments().get( i ) ) );
		}
		return new ClassInstantiationExpression(
				new TypeExpression( n1.typeExpression().name(),
						n1.typeExpression().worldArguments(), n1.typeExpression().typeArguments() ),
				arguments,
				n1.typeArguments()
		);
	}

	@Override
	public Expression merge(
			EnumCaseInstantiationExpression n1, EnumCaseInstantiationExpression n2
	) {
		String errorPrefix = "Cannot merge enum case instantiation expression due to ";
		MergeException._assert(
				n1.name().equals( n2.name() ),
				errorPrefix + "enums with different names: " + n1.name() + " and " + n2.name(), n1,
				n2
		);
		MergeException._assert(
				n1._case().equals( n2._case() ),
				errorPrefix + "enums with different cases: " + n1._case() + " and " + n2._case(),
				n1, n2
		);
		return new EnumCaseInstantiationExpression( n1.name(), n1._case(), n1.world() );
	}

	@Override
	public Expression merge( EnclosedExpression n1, EnclosedExpression n2 ) {
		return new EnclosedExpression(
				merge( n1.nestedExpression(), n2.nestedExpression() )
		);
	}

	@Override
	public Expression merge( FieldAccessExpression n1, FieldAccessExpression n2 ) {
		MergeException._assert(
				n1.name().equals( n2.name() ),
				"Cannot merge field access expressions due to different fields: " + n1.name() + " and " + n2.name(),
				n1, n2
		);
		return new FieldAccessExpression( n1.name() );
	}

	@Override
	public Expression merge( StaticAccessExpression n1, StaticAccessExpression n2 ) {
		MergeException._assert(
				n1.typeExpression().equals( n2.typeExpression() ),
				"Cannot merge static access expressions due to different types: "
						+ ppv.visit( n1.typeExpression() ) + " and " + ppv.visit(
						n2.typeExpression() ), n1, n2
		);
		return new StaticAccessExpression( n1.typeExpression() );
	}

	@Override
	public Expression merge( MethodCallExpression n1, MethodCallExpression n2 ) {
		String errorPrefix = "Cannot merge method call expressions due to ";
		MergeException._assert(
				n1.name().equals( n2.name() ),
				errorPrefix + "different names: " + n1.name() + " and " + n2.name(), n1, n2
		);
		MergeException._assert(
				n1.typeArguments().size() == n2.typeArguments().size(),
				errorPrefix + "different generic argument sizes: " + n1.typeArguments().size() + " and " + n2.typeArguments().size(),
				n1, n2
		);
		for( int i = 0; i < n1.typeArguments().size(); i++ ) {
			MergeException._assert(
					n1.typeArguments().get( i ).equals( n2.typeArguments().get( i ) ),
					errorPrefix + "different class types: " + ppv.visit(
							n1.typeArguments().get( i ) ) + " and " + ppv.visit(
							n2.typeArguments().get( i ) ), n1, n2
			);
		}
		MergeException._assert(
				n1.arguments().size() == n2.arguments().size(),
				errorPrefix + "different argument sizes: " + n1.arguments().size() + " and " + n2.arguments().size(),
				n1, n2
		);
		List< Expression > arguments = new ArrayList<>();
		for( int i = 0; i < n1.arguments().size(); i++ ) {
			arguments.add( merge( n1.arguments().get( i ), n2.arguments().get( i ) ) );
		}
		return new MethodCallExpression( n1.name(), arguments, n1.typeArguments() );
	}

	@Override
	public Expression merge( NotExpression n1, NotExpression n2 ) {
		return new NotExpression( merge( n1.expression(), n2.expression() ) );
	}

	@Override
	public Expression merge( ThisExpression n1, ThisExpression n2 ) {
		return n1;
	}

	@Override
	public Expression merge( LiteralExpression n1, LiteralExpression n2 ) {
		String errorPrefix = "Cannot merge literal expressions due to ";
		MergeException._assert(
				n1.getClass().getCanonicalName().equals( n2.getClass().getCanonicalName() ),
				errorPrefix + "different types: " + n1.getClass().getCanonicalName() + " and " + n2.getClass().getCanonicalName(),
				n1, n2
		);
		MergeException._assert(
				n1.content().equals( n2.content() ),
				errorPrefix + "different values: " + n1.content() + " and " + n2.content(), n1, n2
		);
		return n1;
	}

	@Override
	public Expression merge( ScopedExpression n1, ScopedExpression n2 ) {
		return new ScopedExpression(
				merge( n1.scope(), n2.scope() ),
				merge( n1.scopedExpression(), n2.scopedExpression() )
		);
	}


}
