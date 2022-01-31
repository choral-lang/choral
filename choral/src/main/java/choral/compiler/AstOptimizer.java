/*
 *     Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 *     Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 *     Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU Library General Public License as
 *     published by the Free Software Foundation; either version 2 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU Library General Public
 *     License along with this program; if not, write to the
 *     Free Software Foundation, Inc.,
 *     59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package choral.compiler;

import choral.ast.*;
import choral.ast.body.Class;
import choral.ast.body.Enum;
import choral.ast.body.*;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.exceptions.SyntaxException;
import choral.grammar.ChoralParser;
import choral.grammar.ChoralVisitor;
import choral.utils.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AstOptimizer implements ChoralVisitor {

	// The choral compilation unit
	private final List< ImportDeclaration > imports;
	private final List< Interface > interfaces;
	private final List< Class > classes;
	private final List< Enum > enums;
	private String file = "";
	private String _package = "";
	private final Set< String > debugExcludeMethods = new HashSet<>();
	private boolean debug;
	private String lastMethod = "";

	/* * * * * * TOP-LEVEL COMPILATION UNIT ELEMENTS * * * * * */
	private int cmCounter = 0;

	private AstOptimizer() {
		this.imports = new ArrayList<>();
		this.interfaces = new ArrayList<>();
		this.classes = new ArrayList<>();
		this.enums = new ArrayList<>();
		this.debug = false;
	}

	public static AstOptimizer loadParameters( String... params ) {
		AstOptimizer ao = new AstOptimizer();
		Set< String > p = Set.of( params );
		if( p.contains( "showDebug" ) ) {
			ao.debug = true;
		}
		return ao;
	}

	public static AstOptimizer showDebug() {
		AstOptimizer ao = new AstOptimizer();
		ao.debug = true;
		return ao;
	}

	public CompilationUnit optimise( ChoralParser.CompilationUnitContext cu, String file ) {
		this.file = file;
		return visitCompilationUnit( cu );
	}

//    public CompilationUnit optimise(ChoralParser.CompilationUnitContext cu) {
//        return optimise(cu, "");
//    }


	/* * * * * * IMPORT TYPENAMES * * * * * * */

	@Override
	public String visitQualifiedName( ChoralParser.QualifiedNameContext qn ) {
		debugInfo();
		return ifPresent( qn.qualifiedName() ).applyOrElse( q -> visitQualifiedName( q ) + ".",
				String::new ) +
				qn.Identifier().getText();
	}

	/* * * * * * WORLDS AND TYPES PARAMETERS * * * * * */

	@Override
	public CompilationUnit visitCompilationUnit( ChoralParser.CompilationUnitContext cu ) {
		debugInfo();
		ifPresent( cu.headerDeclaration() ).apply( this::visitHeaderDeclaration );
		cu.typeDeclaration().forEach( this::visitTypeDeclaration );
		flushDebug();
		return new CompilationUnit( _package, imports, interfaces, classes, enums, this.file );
	}

	@Override
	public Void visitHeaderDeclaration( ChoralParser.HeaderDeclarationContext hdc ) {
		debugInfo();
		ifPresent( hdc.packageDeclaration() ).apply( this::visitPackageDeclaration );
		hdc.importDeclaration().forEach( this::visitImportDeclaration );
		return null;
	}

	@Override
	public Void visitPackageDeclaration( ChoralParser.PackageDeclarationContext pdc ) {
		debugInfo();
		_package = visitQualifiedName( pdc.qualifiedName() );
		return null;
	}

	@Override
	public Void visitImportDeclaration( ChoralParser.ImportDeclarationContext idc ) {
		debugInfo();
		ImportDeclaration i = new ImportDeclaration(
				visitQualifiedName( idc.qualifiedName() ) + ( ( idc.STAR() == null ) ? "" : ".*" ),
				getPosition( idc )
		);
		imports.add( i );
		return null;
	}

	@Override
	public Void visitTypeDeclaration( ChoralParser.TypeDeclarationContext tdc ) {
		debugInfo();
		ifPresent( tdc.classDeclaration() ).apply( this::visitClassDeclaration );
		ifPresent( tdc.interfaceDeclaration() ).apply( this::visitInterfaceDeclaration );
		ifPresent( tdc.enumDeclaration() ).apply( this::visitEnumDeclaration );
		return null;
	}

	@Override
	public Annotation visitAnnotation( ChoralParser.AnnotationContext ac ) {
		debugInfo();

		Map< Name, LiteralExpression > annotationValues = new HashMap<>();
		if( isPresent( ac.literal() ) ) {
			// Annotations declared with just the param "value" can be instantiated with a literal
			// Desugar this by hardcoding the name "value"
			annotationValues.put( new Name( "value", getPosition( ac.literal() ) ),
					visitLiteral( ac.literal(), null ) );
		} else if( isPresent( ac.annotationValues() ) ) {
			// Other annotations with attributes must be instantiated with a name=value list
			annotationValues.putAll( visitAnnotationValues( ac.annotationValues() ) );
		}

		return new Annotation(
				getName( ac.Identifier() ),
				annotationValues,
				getPosition( ac )
		);
	}

	@Override
	public Map< Name, LiteralExpression > visitAnnotationValues(
			ChoralParser.AnnotationValuesContext avc
	) {
		debugInfo();
		Map< Name, LiteralExpression > vm = Collections.singletonMap(
				getName( avc.Identifier() ),
				visitLiteral( avc.literal(), null )
		);
		if( isPresent( avc.annotationValues() ) ) {
			vm.putAll( visitAnnotationValues( avc.annotationValues() ) );
		}
		return vm;
	}

	@Override
	public Void visitClassDeclaration( ChoralParser.ClassDeclarationContext cls ) {
		debugInfo();
		Name name = getName( cls.Identifier() );

		// Retrieve modifiers
		EnumSet< ClassModifier > modifiers = EnumSet.noneOf( ClassModifier.class );
		for( ChoralParser.ClassModifierContext ctx : cls.classModifier() ) {
			ClassModifier m = visitClassModifier( ctx );
			if( modifiers.contains( m ) ) {
				throw new SyntaxException( getPosition( ctx ),
						"Illegal combination of modifiers '" + m.label + "' and '" + m.label + "'." );
			} else {
				modifiers.add( m );
			}
		}

		// Retrieve the world parameters
		List< FormalWorldParameter > worldParameters = ifPresent( cls.worldParameters() )
				.applyOrElse( this::visitWorldParameters, Collections::emptyList );

		// Retrieve the type parameters (with bound and unbound worlds)
		List< FormalTypeParameter > typeParameters = ifPresent( cls.typeParameters() )
				.applyOrElse( this::visitTypeParameters, Collections::emptyList );

		// Retrieve the interfaces it implements
		List< TypeExpression > interfaces = ifPresent( cls.superInterfaces() )
				.applyOrElse( this::visitSuperInterfaces, Collections::emptyList );

		// Retrieve fields and methods
		Class headlessClass = visitClassBody( cls.classBody() );
		// we need to check that constructors have the expected name
		for( ConstructorDefinition c : headlessClass.constructors() ) {
			if( !c.signature().name().equals( name ) ) {
				throw new SyntaxException( c.position(),
						"Ambiguous method or constructor declaration: methods must have a return type and constructors must have the same name of the defining class." );
			}
		}
		Class c = new Class( name,
				worldParameters,
				typeParameters,
				ifPresent( cls.superClass() ).applyOrElse( this::visitSuperClass, () -> null ),
				interfaces,
				headlessClass.fields(),
				headlessClass.methods(),
				headlessClass.constructors(),
				cls.annotation().stream().map( this::visitAnnotation ).collect(
						Collectors.toList() ),
				modifiers,
				getPosition( cls )
		);
		classes.add( c );
		return null;
	}

	@Override
	public ClassModifier visitClassModifier( ChoralParser.ClassModifierContext ctx ) {
		return ClassModifier.valueOf( ctx.getText().toUpperCase() );
	}

	@Override
	public Void visitInterfaceDeclaration( ChoralParser.InterfaceDeclarationContext id ) {
		debugInfo();
		Name name = getName( id.Identifier() );

		// Retrieve modifiers
		EnumSet< InterfaceModifier > modifiers = EnumSet.noneOf( InterfaceModifier.class );
		for( ChoralParser.InterfaceModifierContext ctx : id.interfaceModifier() ) {
			InterfaceModifier m = visitInterfaceModifier( ctx );
			if( modifiers.contains( m ) ) {
				throw new SyntaxException( getPosition( ctx ),
						"Illegal combination of modifiers '" + m.label + "' and '" + m.label + "'." );
			} else {
				modifiers.add( m );
			}
		}

		// Retrieve the world parameters
		List< FormalWorldParameter > worldParameters = ifPresent( id.worldParameters() )
				.applyOrElse( this::visitWorldParameters, Collections::emptyList );

		// Retrieve the type parameters (with unbound worlds)
		List< FormalTypeParameter > typeParameters = ifPresent( id.typeParameters() )
				.applyOrElse( this::visitTypeParameters, Collections::emptyList );

		// Retrieve the interfaces it extends
		List< TypeExpression > superTypes = ifPresent( id.extendsInterfaces() )
				.applyOrElse( this::visitExtendsInterfaces, Collections::emptyList );

		List< InterfaceMethodDefinition > methods = visitInterfaceBody( id.interfaceBody() );
		Interface i = new Interface(
				name,
				worldParameters,
				typeParameters,
				superTypes,
				methods,
				id.annotation().stream().map( this::visitAnnotation ).collect(
						Collectors.toList() ),
				modifiers,
				getPosition( id )
		);
		interfaces.add( i );
		return null;
	}

	@Override
	public InterfaceModifier visitInterfaceModifier( ChoralParser.InterfaceModifierContext ctx ) {
		return InterfaceModifier.valueOf( ctx.getText().toUpperCase() );
	}

	@Override
	public Void visitEnumDeclaration( ChoralParser.EnumDeclarationContext ed ) {
		debugInfo();
		Name name = getName( ed.Identifier() );

		// Retrieve modifiers
		EnumSet< ClassModifier > modifiers = EnumSet.noneOf( ClassModifier.class );
		for( ChoralParser.ClassModifierContext ctx : ed.classModifier() ) {
			ClassModifier m = visitClassModifier( ctx );
			if( modifiers.contains( m ) ) {
				throw new SyntaxException( getPosition( ctx ),
						"Illegal combination of modifiers '" + m.label + "' and '" + m.label + "'." );
			} else {
				modifiers.add( m );
			}
		}

		Enum e = new Enum(
				name,
				visitWorldParameter( ed.worldParameter() ),
				visitEnumBody( ed.enumBody() ),
				ed.annotation().stream().map( this::visitAnnotation ).collect(
						Collectors.toList() ),
				modifiers,
				getPosition( ed )
		);
		enums.add( e );
		return null;
	}

	@Override
	public List< WorldArgument > visitWorldArguments( ChoralParser.WorldArgumentsContext wps ) {
		debugInfo();
		return ifPresent( wps.worldArgument() ).applyOrElse(
				w -> Collections.singletonList( visitWorldArgument( w ) ),
				() -> visitWorldArgumentList( wps.worldArgumentList() )
		);
	}


	@Override
	public List< WorldArgument > visitWorldArgumentList(
			ChoralParser.WorldArgumentListContext wpl
	) {
		debugInfo();
		return wpl.worldArgument().stream()
				.map( this::visitWorldArgument )
				.collect( Collectors.toList() );
	}

	@Override
	public WorldArgument visitWorldArgument( ChoralParser.WorldArgumentContext wp ) {
		debugInfo();
		return new WorldArgument(
				getName( wp.Identifier() ),
				getPosition( wp )
		);
	}

	@Override
	public List< FormalWorldParameter > visitWorldParameters(
			ChoralParser.WorldParametersContext wps
	) {
		debugInfo();
		return ifPresent( wps.worldParameter() ).applyOrElse(
				w -> Collections.singletonList( visitWorldParameter( w ) ),
				() -> visitWorldParameterList( wps.worldParameterList() )
		);
	}

	@Override
	public List< FormalWorldParameter > visitWorldParameterList(
			ChoralParser.WorldParameterListContext wpl
	) {
		debugInfo();
		return wpl.worldParameter().stream()
				.map( this::visitWorldParameter )
				.collect( Collectors.toList() );
	}

	@Override
	public FormalWorldParameter visitWorldParameter( ChoralParser.WorldParameterContext wp ) {
		debugInfo();
		return new FormalWorldParameter(
				getName( wp.Identifier() ),
				getPosition( wp ) );
	}

	@Override
	public List< FormalTypeParameter > visitTypeParameters(
			ChoralParser.TypeParametersContext tpc
	) {
		debugInfo();
		return visitTypeParameterList( tpc.typeParameterList() );
	}

	@Override
	public List< FormalTypeParameter > visitTypeParameterList(
			ChoralParser.TypeParameterListContext tpl
	) {
		debugInfo();
		return tpl.typeParameter().stream()
				.map( this::visitTypeParameter )
				.collect( Collectors.toList() );
	}

	@Override
	public FormalTypeParameter visitTypeParameter( ChoralParser.TypeParameterContext tp ) {
		debugInfo();
		Name name = getName( tp.Identifier() );
		List< FormalWorldParameter > worldParameters = ifPresent( tp.worldParameters() )
				.applyOrElse( this::visitWorldParameters, Collections::emptyList );
		List< TypeExpression > superTypes = ifPresent( tp.typeBound() )
				.applyOrElse( this::visitTypeBound, Collections::emptyList );
		return new FormalTypeParameter( name, worldParameters, superTypes, getPosition( tp ) );
	}

	@Override
	public List< TypeExpression > visitTypeBound( ChoralParser.TypeBoundContext tb ) {
		debugInfo();
		List< TypeExpression > ftpl = tb.additionalBound().stream()
				.map( this::visitAdditionalBound )
				.collect( Collectors.toList() );
		ftpl.add( visitReferenceType( tb.referenceType() ) );
		return ftpl;
	}

	/* * * * * * * * INTERFACE DECLARATION (EXTENDS) * * * * * * */

	@Override
	public TypeExpression visitAdditionalBound( ChoralParser.AdditionalBoundContext ab ) {
		debugInfo();
		return visitReferenceType( ab.referenceType() );
	}

	@Override
	public List< TypeExpression > visitTypeArguments( ChoralParser.TypeArgumentsContext ta ) {
		debugInfo();
		return visitTypeArgumentList( ta.typeArgumentList() );
	}

	@Override
	public List< TypeExpression > visitTypeArgumentList(
			ChoralParser.TypeArgumentListContext tal
	) {
		debugInfo();
		return tal.referenceType().stream().map( this::visitReferenceType ).collect(
				Collectors.toList() );
	}

	/* * * * * * * * CLASS BODIES * * * * * */

	@Override
	public TypeExpression visitReferenceType( ChoralParser.ReferenceTypeContext rt ) {
		debugInfo();
		List< WorldArgument > worlds = ifPresent( rt.worldArguments() )
				.applyOrElse( this::visitWorldArguments, Collections::emptyList );
		List< TypeExpression > parameters = ifPresent( rt.typeArguments() )
				.applyOrElse( this::visitTypeArguments, Collections::emptyList );
		return new TypeExpression( getName( rt.Identifier() ), worlds, parameters,
				getPosition( rt ) );
	}

	@Override
	public List< TypeExpression > visitSuperInterfaces( ChoralParser.SuperInterfacesContext si ) {
		debugInfo();
		return visitInterfaceTypeList( si.interfaceTypeList() );
	}

	@Override
	public List< TypeExpression > visitExtendsInterfaces(
			ChoralParser.ExtendsInterfacesContext ei
	) {
		debugInfo();
		return visitInterfaceTypeList( ei.interfaceTypeList() );
	}

	@Override
	public List< TypeExpression > visitInterfaceTypeList(
			ChoralParser.InterfaceTypeListContext itl
	) {
		debugInfo();
		return itl.referenceType().stream()
				.map( t -> visitReferenceType( t ) ).collect( Collectors.toList() );
	}

	@Override
	public TypeExpression visitSuperClass( ChoralParser.SuperClassContext scc ) {
		debugInfo();
		return visitReferenceType( scc.referenceType() );
	}

	@Override
	public Class visitClassBody( ChoralParser.ClassBodyContext cb ) {
		debugInfo();

		List< Node > bodyMembers = ifPresent( cb.classBodyDeclaration() ).applyOrElse(
				el -> el.stream().flatMap(
								e -> visitClassBodyDeclaration( e ).stream() )
						.collect( Collectors.toList() ),
				Collections::emptyList );
		List< Field > fields = bodyMembers.stream()
				.filter( Field.class::isInstance )
				.map( Field.class::cast )
				.collect( Collectors.toList() );
		List< ClassMethodDefinition > methods = bodyMembers.stream()
				.filter( ClassMethodDefinition.class::isInstance )
				.map( ClassMethodDefinition.class::cast )
				.collect( Collectors.toList() );
		List< ConstructorDefinition > constructors = bodyMembers.stream()
				.filter( ConstructorDefinition.class::isInstance )
				.map( ConstructorDefinition.class::cast )
				.collect( Collectors.toList() );
		return new Class( null, null, null, null, null, fields, methods, constructors,
				Collections.emptyList(), null, null );
		// INFO: headless class, the line and column are associated with the signature. above
	}

	@Override
	public List< Node > visitClassBodyDeclaration( ChoralParser.ClassBodyDeclarationContext cbd ) {
		debugInfo();
		return ifPresent( cbd.classMemberDeclaration() ).applyOrElse(
				this::visitClassMemberDeclaration,
				() -> Collections.singletonList(
						visitConstructorDeclaration( cbd.constructorDeclaration() ) )
		);
	}

	@Override
	public List< Node > visitClassMemberDeclaration(
			ChoralParser.ClassMemberDeclarationContext cmd
	) {
		debugInfo();
		if( cmd.fieldDeclaration() != null ) {
			return visitFieldDeclaration( cmd.fieldDeclaration() );
		} else {
			return Collections.singletonList( visitMethodDeclaration( cmd.methodDeclaration() ) );
		}
	}

	@Override
	public List< Node > visitFieldDeclaration( ChoralParser.FieldDeclarationContext fd ) {
		debugInfo();

		// Retrieve modifiers
		EnumSet< FieldModifier > modifiers = EnumSet.noneOf( FieldModifier.class );
		for( ChoralParser.FieldModifierContext ctx : fd.fieldModifier() ) {
			FieldModifier m = visitFieldModifier( ctx );
			if( modifiers.contains( m ) ) {
				throw new SyntaxException( getPosition( ctx ),
						"Illegal combination of modifiers '" + m.label + "' and '" + m.label + "'." );
			} else {
				modifiers.add( m );
			}
		}

		return fd.Identifier().stream().map( f -> new Field(
				getName( f ),
				visitReferenceType( fd.referenceType() ),
				modifiers,
				getPosition( fd ) )
		).collect( Collectors.toList() );
	}

	@Override
	public FieldModifier visitFieldModifier( ChoralParser.FieldModifierContext ctx ) {
		return FieldModifier.valueOf( ctx.getText().toUpperCase() );
	}

	@Override
	public MethodSignature visitMethodHeader( ChoralParser.MethodHeaderContext mh ) {
		debugInfo();
		return new MethodSignature(
				getName( mh.Identifier() ),
				ifPresent( mh.typeParameters() ).applyOrElse( this::visitTypeParameters,
						Collections::emptyList ),
				ifPresent( mh.formalParameterList() ).applyOrElse( this::visitFormalParameterList,
						Collections::emptyList ),
				visitResult( mh.result() ),
				getPosition( mh )
		);
	}

	@Override
	public TypeExpression visitResult( ChoralParser.ResultContext r ) {
		debugInfo();
		return ifPresent( r.referenceType() ).applyOrElse(
				this::visitReferenceType,
				() -> new TypeExpression( new Name( "void" ), Collections.emptyList(),
						Collections.emptyList() )
		);
	}

	@Override
	public Node visitMethodDeclaration( ChoralParser.MethodDeclarationContext md ) {
		debugInfo();
		// Retrieve modifiers
		EnumSet< ClassMethodModifier > modifiers = EnumSet.noneOf( ClassMethodModifier.class );
		for( ChoralParser.MethodModifierContext ctx : md.methodModifier() ) {
			ClassMethodModifier m = visitMethodModifier( ctx );
			if( modifiers.contains( m ) ) {
				throw new SyntaxException( getPosition( ctx ),
						"Illegal combination of modifiers '" + m.label + "' and '" + m.label + "'." );
			} else {
				modifiers.add( m );
			}
		}

		return new ClassMethodDefinition(
				visitMethodHeader( md.methodHeader() ),
				visitMethodBody( md.methodBody() ).orElse( null ),
				md.annotation().stream().map( this::visitAnnotation ).collect(
						Collectors.toList() ),
				modifiers,
				getPosition( md )
		);
	}


	@Override
	public ClassMethodModifier visitMethodModifier( ChoralParser.MethodModifierContext ctx ) {
		return ClassMethodModifier.valueOf( ctx.getText().toUpperCase() );
	}

	@Override
	public List< FormalMethodParameter > visitFormalParameterList(
			ChoralParser.FormalParameterListContext fpl
	) {
		debugInfo();
		return visitFormalParameters( fpl.formalParameters() );
	}

	/* * * * * * * * * * INTERFACE BODIES  * * * * * * * * */

	@Override
	public List< FormalMethodParameter > visitFormalParameters(
			ChoralParser.FormalParametersContext fps
	) {
		debugInfo();
		return fps.formalParameter().stream().map( this::visitFormalParameter ).collect(
				Collectors.toList() );
	}

	@Override
	public FormalMethodParameter visitFormalParameter( ChoralParser.FormalParameterContext fp ) {
		debugInfo();
		return new FormalMethodParameter(
				getName( fp.Identifier() ),
				visitReferenceType( fp.referenceType() ),
				getPosition( fp )
		);
	}

	/* * * * * * * * * * ENUM BODIES  * * * * * * * * */

	@Override
	public ConstructorDefinition visitConstructorDeclaration(
			ChoralParser.ConstructorDeclarationContext cd
	) {
		debugInfo();

		// Retrieve modifiers
		EnumSet< ConstructorModifier > modifiers = EnumSet.noneOf( ConstructorModifier.class );
		for( ChoralParser.ConstructorModifierContext ctx : cd.constructorModifier() ) {
			ConstructorModifier m = visitConstructorModifier( ctx );
			if( modifiers.contains( m ) ) {
				throw new SyntaxException( getPosition( ctx ),
						"Illegal combination of modifiers '" + m.label + "' and '" + m.label + "'." );
			} else {
				modifiers.add( m );
			}
		}
		Pair< MethodCallExpression, Statement > b = visitConstructorBody( cd.constructorBody() );
		return new ConstructorDefinition(
				visitConstructorDeclarator( cd.constructorDeclarator() ),
				b.left(),
				b.right(),
				modifiers,
				getPosition( cd ) );
	}

	@Override
	public ConstructorModifier visitConstructorModifier(
			ChoralParser.ConstructorModifierContext ctx
	) {
		return ConstructorModifier.valueOf( ctx.getText().toUpperCase() );
	}

	@Override
	public ConstructorSignature visitConstructorDeclarator(
			ChoralParser.ConstructorDeclaratorContext cd
	) {
		debugInfo();
		return new ConstructorSignature(
				getName( cd.Identifier() ),
				ifPresent( cd.typeParameters() ).applyOrElse(
						this::visitTypeParameters,
						Collections::emptyList
				),
				ifPresent( cd.formalParameterList() ).applyOrElse(
						this::visitFormalParameterList,
						Collections::emptyList
				),
				getPosition( cd )
		);
	}

	/* * * * * * * * * * * CONSTRUCTOR AND METHOD BODIES (STATEMENTS BLOCKS ETC) * * * * * * * * * * * * * */

	@Override
	public Pair< MethodCallExpression, Statement > visitConstructorBody(
			ChoralParser.ConstructorBodyContext ctx
	) {
		debugInfo();
		return new Pair<>(
				( ctx.explicitConstructorInvocation() == null )
						? null
						: visitExplicitConstructorInvocation( ctx.explicitConstructorInvocation() ),
				visitBlockStatements( ctx.blockStatements() )
		);
	}

	@Override
	public MethodCallExpression visitExplicitConstructorInvocation(
			ChoralParser.ExplicitConstructorInvocationContext ctx
	) {
		return new MethodCallExpression(
				getName( ( ctx.SUPER() == null ) ? ctx.THIS() : ctx.SUPER() ),
				ifPresent( ctx.argumentList() )
						.apply( this::visitArgumentList )
						.orElse( List.of() ),
				ifPresent( ctx.typeArguments() )
						.apply( this::visitTypeArguments )
						.orElse( List.of() ),
				getPosition( ctx ) );
	}

	@Override
	public List< InterfaceMethodDefinition > visitInterfaceBody(
			ChoralParser.InterfaceBodyContext ib
	) {
		debugInfo();
		return ib.interfaceMethodDeclaration().stream().map(
				this::visitInterfaceMethodDeclaration ).collect( Collectors.toList() );

	}

	@Override
	public InterfaceMethodDefinition visitInterfaceMethodDeclaration(
			ChoralParser.InterfaceMethodDeclarationContext imd
	) {
		debugInfo();
		// Retrieve modifiers
		EnumSet< InterfaceMethodModifier > modifiers = EnumSet.noneOf(
				InterfaceMethodModifier.class );
		for( ChoralParser.InterfaceMethodModifierContext ctx : imd.interfaceMethodModifier() ) {
			InterfaceMethodModifier m = visitInterfaceMethodModifier( ctx );
			if( modifiers.contains( m ) ) {
				throw new SyntaxException( getPosition( ctx ),
						"Illegal combination of modifiers '" + m.label + "' and '" + m.label + "'." );
			} else {
				modifiers.add( m );
			}
		}

		return new InterfaceMethodDefinition(
				visitMethodHeader( imd.methodHeader() ),
				imd.annotation().stream().map( this::visitAnnotation ).collect(
						Collectors.toList() ),
				modifiers,
				getPosition( imd )
		);
	}

	@Override
	public InterfaceMethodModifier visitInterfaceMethodModifier(
			ChoralParser.InterfaceMethodModifierContext ctx
	) {
		return InterfaceMethodModifier.valueOf( ctx.getText().toUpperCase() );
	}

	@Override
	public List< Name > visitEnumBody( ChoralParser.EnumBodyContext eb ) {
		debugInfo();
		return ifPresent( eb.enumConstantList() ).applyOrElse(
				this::visitEnumConstantList,
				Collections::emptyList
		);
	}

	/* * * * * * * * * * STATEMENTS * * * * * * * * * * * * */

	@Override
	public List< Name > visitEnumConstantList( ChoralParser.EnumConstantListContext ctx ) {
		return ctx.enumConstant().stream().map( this::visitEnumConstant ).collect(
				Collectors.toList() );
	}

	@Override
	public Name visitEnumConstant( ChoralParser.EnumConstantContext ctx ) {
		debugInfo();
		return getName( ctx.Identifier() );
	}

	@Override
	public Optional< Statement > visitMethodBody( ChoralParser.MethodBodyContext mb ) {
		debugInfo();
		return Optional.ofNullable( mb.block() ).map( this::visitBlock );
	}

	@Override
	public Statement visitBlock( ChoralParser.BlockContext b ) {
		debugInfo();
		return ifPresent( b.blockStatements() ).applyOrElse(
				this::visitBlockStatements,
				() -> new NilStatement( getPosition( b ) )
		);
	}

	@Override
	public Statement visitBlockStatements( ChoralParser.BlockStatementsContext bss ) {
		debugInfo();
		if( isPresent( bss ) ) {
			List< ChoralParser.BlockStatementContext > statements = new ArrayList<>(
					bss.blockStatement() );
			Collections.reverse( statements );
			Statement chainedStatement = new NilStatement(
					getPosition( bss ) ); // INFO: no line and column here, using bss
			for( ChoralParser.BlockStatementContext s : statements ) {
				chainedStatement = visitBlockStatement( s ).cloneWithContinuation(
						chainedStatement );
			}
			return chainedStatement;
		} else {
			return new NilStatement();
		}
	}

	@Override
	public Statement visitBlockStatement( ChoralParser.BlockStatementContext bsc ) {
		debugInfo();
		if( isPresent( bsc.localVariableDeclaration() ) ) {
			return visitLocalVariableDeclaration( bsc.localVariableDeclaration() );
		}
		if( isPresent( bsc.localVariableDeclarationAndAssignment() ) ) {
			return visitLocalVariableDeclarationAndAssignment(
					bsc.localVariableDeclarationAndAssignment() );
		} else if( isPresent( bsc.block() ) ) {
			return new BlockStatement( visitBlock( bsc.block() ), null, getPosition( bsc ) );
		} else {
			return visitStatement( bsc.statement() );
		}
	}

	@Override
	public Statement visitStatement( ChoralParser.StatementContext s ) {
		debugInfo();
		if( isPresent( s.basicStatement() ) ) {
			return visitBasicStatement( s.basicStatement() );
		} else if( isPresent( s.ifThenStatement() ) ) {
			return visitIfThenStatement( s.ifThenStatement() );
		} else if( isPresent( s.ifThenElseStatement() ) ) {
			return visitIfThenElseStatement( s.ifThenElseStatement() );
		} else if( isPresent( s.switchStatement() ) ) {
			return this.visitSwitchStatement( s.switchStatement() );
		} else if( isPresent( s.tryCatchStatement() ) ) {
			return visitTryCatchStatement( s.tryCatchStatement() );
		} else {
			return new NilStatement( getPosition( s ) );
		}
	}

	@Override
	public Statement visitLocalVariableDeclaration(
			ChoralParser.LocalVariableDeclarationContext lvd
	) {
		debugInfo();
		TypeExpression t = visitReferenceType( lvd.referenceType() );
		return new VariableDeclarationStatement(
				lvd.Identifier().stream()
						.map( e -> new VariableDeclaration(
								getName( e ),
								t,
								t.position()
						) )
						.collect( Collectors.toList() ),
				null,
				getPosition( lvd ) );
	}

	@Override
	public Statement visitLocalVariableDeclarationAndAssignment(
			ChoralParser.LocalVariableDeclarationAndAssignmentContext lvac
	) {
		debugInfo();
		TypeExpression t = visitReferenceType( lvac.referenceType() );
		return new VariableDeclarationStatement(
				Collections.singletonList(
						new VariableDeclaration(
								getName( lvac.Identifier() ),
								t,
								t.position()
						) ),
				new ExpressionStatement(
						new AssignExpression(
								ifPresent( lvac.chainedExpression() ).applyOrElse(
										ce -> this.visitChainedExpression( ce,
												visitShortCircuitOrExpression(
														lvac.shortCircuitOrExpression() ) ),
										() -> visitShortCircuitOrExpression(
												lvac.shortCircuitOrExpression() )
								),
								new FieldAccessExpression(
										getName( lvac.Identifier() ),
										getPosition( lvac.Identifier() )
								),
								visitAssignmentOperator( lvac.assignmentOperator() ),
								getPosition( lvac ) ),
						null,
						getPosition( lvac ) ),
				getPosition( lvac ) );
	}

	@Override
	public Statement visitBasicStatement( ChoralParser.BasicStatementContext bs ) {
		debugInfo();
		if( isPresent( bs.emptyStatement() ) ) {
			return visitEmptyStatement( bs.emptyStatement() );
//		} else if( isPresent( bs.selectStatement() ) ) {
//			return visitSelectStatement( bs.selectStatement() );
		} else if( isPresent( bs.expressionStatement() ) ) {
			return visitExpressionStatement( bs.expressionStatement() );
		} else {
			return visitReturnStatement( bs.returnStatement() );
		}
	}

	@Override
	public NilStatement visitEmptyStatement( ChoralParser.EmptyStatementContext es ) {
		debugInfo();
		return new NilStatement( getPosition( es ) );
	}

//	@Override
//	public SelectStatement visitSelectStatement( ChoralParser.SelectStatementContext nsc ) {
//		return new SelectStatement(
//				visitEnumCaseCreationExpression( nsc.enumCaseCreationExpression() ),
//				visitExpression( nsc.expression() ),
//				null,
//				getPosition( nsc )
//		);
//	}

	@Override
	public ExpressionStatement visitExpressionStatement(
			ChoralParser.ExpressionStatementContext es
	) {
		debugInfo();
		return new ExpressionStatement(
				isPresent( es.statementExpression() ) ?
						visitStatementExpression( es.statementExpression() )
						: visitChainedExpression( es.chainedExpression(),
						visitExpression( es.expression() ) ),
				null,
				getPosition( es ) );
	}

	@Override
	public Expression visitStatementExpression( ChoralParser.StatementExpressionContext se ) {
		debugInfo();
		if( isPresent( se.assignment() ) ) {
			return visitAssignment( se.assignment() );
		} else {
			return visitTrailingExpression( se.trailingExpression() );
		}
	}

	@Override
	public Expression visitTrailingExpression( ChoralParser.TrailingExpressionContext te ) {
		debugInfo();
		Expression e;
		if( isPresent( te.fieldAccess() ) ) {
			e = visitFieldAccess( te.fieldAccess() );
		} else if( isPresent( te.methodInvocation() ) ) {
			e = visitMethodInvocation( te.methodInvocation() );
		} else if( isPresent( te.staticGenericAccess() ) ) {
			e = visitStaticGenericAccess( te.staticGenericAccess() );
		} else if( isPresent( te.thisOrSuperMethodAccess() ) ) {
			e = visitThisOrSuperMethodAccess( te.thisOrSuperMethodAccess() );
		} else {
			e = visitClassInstanceCreationExpression( te.classInstanceCreationExpression() );
		}
		return ifPresent( te.trailExpression() )
				.applyOrElse(
						_e -> new ScopedExpression( e, visitTrailExpression( _e ), e.position() ),
						() -> e );
	}

	@Override
	public Expression visitFieldAccess( ChoralParser.FieldAccessContext fa ) {
		debugInfo();
		FieldAccessExpression fieldAccessExpr = new FieldAccessExpression(
				getName( fa.Identifier() ), getPosition( fa.Identifier() ) );
		if( isPresent( fa.primary() ) ) {
			return new ScopedExpression( visitPrimary( fa.primary() ), fieldAccessExpr,
					fieldAccessExpr.position() );
		} else {
			return ifPresent( fa.expressionName() ).applyOrElse(
					e -> new ScopedExpression( visitExpressionName( e ), fieldAccessExpr,
							fieldAccessExpr.position() ),
					() -> fieldAccessExpr
			);
		}
	}

	@Override
	public Expression visitFieldAccess_no_primary(
			ChoralParser.FieldAccess_no_primaryContext fanp
	) {
		FieldAccessExpression fieldAccessExpr = new FieldAccessExpression(
				getName( fanp.Identifier() ), getPosition( fanp.Identifier() ) );
		return ifPresent( fanp.expressionName() ).applyOrElse(
				e -> new ScopedExpression( visitExpressionName( e ), fieldAccessExpr,
						fieldAccessExpr.position() ),
				() -> fieldAccessExpr
		);
	}

	@Override
	public Expression visitTrailExpression( ChoralParser.TrailExpressionContext te ) {
		debugInfo();
		Expression e;
		if( isPresent( te.fieldAccess_no_primary() ) ) {
			e = visitFieldAccess_no_primary( te.fieldAccess_no_primary() );
		} else {
			e = visitMethodInvocation( te.methodInvocation() );
		}
		return ifPresent( te.trailExpression() ).applyOrElse(
				( t ) -> new ScopedExpression( e, visitTrailExpression( te.trailExpression() ),
						e.position() ),
				() -> e
		);
	}

	@Override
	public Expression visitThisOrSuperMethodAccess(
			ChoralParser.ThisOrSuperMethodAccessContext tosma
	) {
		debugInfo();
		Expression e = new ScopedExpression(
				tosma.superSymbol != null ?
						new SuperExpression( getPosition( tosma.superSymbol ) )
						: new ThisExpression( getPosition( tosma.thisSymbol ) ),
				visitMethodInvocation( tosma.methodInvocation() )
		);
		e.setPosition( getPosition( tosma ) );
		return e;
	}

	@Override
	public Object visitChainedExpression( ChoralParser.ChainedExpressionContext ce ) {
		throw new UnsupportedOperationException();
	}

	public Expression visitChainedExpression(
			ChoralParser.ChainedExpressionContext ce, Expression expression
	) {
		debugInfo();
		List< Pair< Expression, ? extends InvocationExpression > > invocationChain =
				ce.chainedInvocation().stream().map( this::visitChainedInvocation ).collect(
						Collectors.toList() );
		for( Pair< Expression, ? extends InvocationExpression > p : invocationChain ) {
			if( p.right() instanceof ClassInstantiationExpression ) {
				ClassInstantiationExpression c = (ClassInstantiationExpression) p.right();
				expression = new ClassInstantiationExpression( c.typeExpression(),
						Collections.singletonList( expression ), c.typeArguments(), c.position() );
			} else {
				expression = new MethodCallExpression(
						( (MethodCallExpression) p.right() ).name(),
						Collections.singletonList( expression ),
						p.right().typeArguments(),
						p.right().position()
				);
			}
			if( p.left() != null ) {
				expression = new ScopedExpression( p.left(), expression, p.left().position() );
			}
		}
		return expression;
	}

	@Override
	// the expression leading to the method call, the method call
	public Pair< Expression, ? extends InvocationExpression > visitChainedInvocation(
			ChoralParser.ChainedInvocationContext ci
	) {
		debugInfo();
		if( isPresent( ci.chainedMethodInvocation() ) ) {
			return visitChainedMethodInvocation( ci.chainedMethodInvocation() );
		} else if( isPresent( ci.chainedStaticMethodInvocation() ) ) {
			return visitChainedStaticMethodInvocation( ci.chainedStaticMethodInvocation() );
		} else {
			return new Pair<>( null,
					visitChainedClassInstanceCreation( ci.chainedClassInstanceCreation() ) );
		}
	}

	@Override
	public Pair< Expression, MethodCallExpression > visitChainedMethodInvocation(
			ChoralParser.ChainedMethodInvocationContext mi
	) {
		debugInfo();
		List< TerminalNode > identifiers = mi.Identifier();
		Collections.reverse( identifiers );
		MethodCallExpression methodCall = new MethodCallExpression(
				getName( identifiers.remove( 0 ) ),
				null,
				ifPresent( mi.typeArguments() ).apply( this::visitTypeArguments ).orElse(
						Collections.emptyList() ),
				getPosition( mi )
		);
		Expression expression;
		if( isPresent( mi.THIS() ) ) {
			expression = new ThisExpression( getPosition( mi ) );
		} else if( isPresent( mi.SUPER() ) ) {
			expression = new SuperExpression( getPosition( mi ) );
		} else {
			expression = new FieldAccessExpression( getName( identifiers.remove( 0 ) ),
					getPosition( mi ) );
		}
		for( TerminalNode identifier : identifiers ) {
			expression = new ScopedExpression(
					new FieldAccessExpression( getName( identifier ), getPosition( mi ) ),
					expression, getPosition( mi ) );
		}
		return new Pair<>( expression, methodCall );
	}

	@Override
	public Pair< Expression, MethodCallExpression > visitChainedStaticMethodInvocation(
			ChoralParser.ChainedStaticMethodInvocationContext smi
	) {
		debugInfo();
		List< Name > identifiers = smi.Identifier().stream().map( this::getName ).collect(
				Collectors.toList() );
		MethodCallExpression mc = new MethodCallExpression(
				identifiers.remove( identifiers.size() - 1 ),
				null,
				ifPresent( smi.typeArguments() ).apply( this::visitTypeArguments ).orElse(
						Collections.emptyList() ),
				getPosition( smi )
		);
		Expression e = visitStaticGenericAccess( smi.staticGenericAccess() );
		for( Name identifier : identifiers ) {
			e = new ScopedExpression( e,
					new FieldAccessExpression( identifier, identifier.position() ),
					getPosition( smi ) );
		}
		return new Pair<>( e, mc );
	}

	@Override
	public ClassInstantiationExpression visitChainedClassInstanceCreation(
			ChoralParser.ChainedClassInstanceCreationContext cic
	) {
		debugInfo();
		TypeExpression e = visitStaticGenericAccess( cic.staticGenericAccess() ).typeExpression();
		return new ClassInstantiationExpression(
				e, null,
				ifPresent( cic.typeArguments() ).applyOrElse( this::visitTypeArguments,
						Collections::emptyList ),
				getPosition( cic )
		);
	}

	@Override
	public StaticAccessExpression visitStaticGenericAccess(
			ChoralParser.StaticGenericAccessContext sga
	) {
		debugInfo();
		return new StaticAccessExpression(
				new TypeExpression(
						getName( sga.Identifier() ),
						ifPresent( sga.worldArguments() ).applyOrElse( this::visitWorldArguments,
								Collections::emptyList ),
						ifPresent( sga.typeArguments() ).applyOrElse( this::visitTypeArguments,
								Collections::emptyList ),
						getPosition( sga )
				),
				getPosition( sga ) );
	}

	@Override
	public MethodCallExpression visitMethodInvocation( ChoralParser.MethodInvocationContext mi ) {
		debugInfo();
		return new MethodCallExpression(
				getName( mi.Identifier() ),
				ifPresent( mi.argumentList() ).applyOrElse( this::visitArgumentList,
						Collections::emptyList ),
				ifPresent( mi.typeArguments() ).apply( this::visitTypeArguments ).orElse(
						Collections.emptyList() ),
				getPosition( mi )
		);
	}

	/* * * * * * * * * ASSIGNMENT * * * * * * * */

	@Override
	public List< Expression > visitArgumentList( ChoralParser.ArgumentListContext al ) {
		debugInfo();
		if( isPresent( al.worldArgumentList() ) ) {
			return visitWorldArgumentList( al.worldArgumentList() )
					.stream()
					.map( w -> visitLiteral( al.literal(), w ) )
					.collect( Collectors.toList() );
		} else {
			return al.expression().stream()
					.map( this::visitExpression )
					.collect( Collectors.toList() );
		}
	}

	@Override
	public AssignExpression visitAssignment( ChoralParser.AssignmentContext a ) {
		debugInfo();
		return new AssignExpression(
				visitExpression( a.expression() ),
				visitLeftHandSide( a.leftHandSide() ),
				visitAssignmentOperator( a.assignmentOperator() ),
				getPosition( a ) );
	}

	/* * * * * * * * * EXPRESSIONS * * * * * * * * * * * */

//	NEW typeArguments? Identifier worldArguments typeArguments? LPAREN argumentList? RPAREN

	@Override
	public ClassInstantiationExpression visitClassInstanceCreationExpression(
			ChoralParser.ClassInstanceCreationExpressionContext cice
	) {
		debugInfo();
		return new ClassInstantiationExpression(
				new TypeExpression(
						new Name( cice.Identifier().getText() ),
						visitWorldArguments( cice.worldArguments() ),
						ifPresent( cice.classArguments ).applyOrElse(
								this::visitTypeArguments, Collections::emptyList
						)
				),
				ifPresent( cice.argumentList() ).applyOrElse( this::visitArgumentList,
						Collections::emptyList ),
				ifPresent( cice.methodArgs ).applyOrElse( this::visitTypeArguments,
						Collections::emptyList ),
				getPosition( cice )
		);
//		return new ClassInstantiationExpression(
//				getName( cice.Identifier() ),
//				ifPresent( cice.worldArguments() ).applyOrElse( this::visitWorldArguments,
//						Collections::emptyList ),
//				ifPresent( cice.argumentList() ).applyOrElse( this::visitArgumentList,
//						Collections::emptyList ),
//				ifPresent( cice.typeArguments() ).applyOrElse( this::visitTypeArguments,
//						Collections::emptyList ),
//				getPosition( cice )
//		);
	}

	/* * * * * * * * * GENERIC EXPRESSIONS * * * * * * * */

	@Override
	public EnumCaseInstantiationExpression visitEnumCaseCreationExpression(
			ChoralParser.EnumCaseCreationExpressionContext ence
	) {
		debugInfo();
		return new EnumCaseInstantiationExpression(
				getName( ence.Identifier( 0 ) ),
				getName( ence.Identifier( 1 ) ),
				visitWorldArgument( ence.worldArgument() ),
				getPosition( ence )
		);
	}

	@Override
	public ReturnStatement visitReturnStatement( ChoralParser.ReturnStatementContext rs ) {
		debugInfo();
		return new ReturnStatement(
				isPresent( rs.chainedExpression() )
						? visitChainedExpression( rs.chainedExpression(),
						visitExpression( rs.expression() ) )
						: isPresent( rs.expression() ) ? visitExpression( rs.expression() )
						: null
				,
				null,
				getPosition( rs ) );
	}

	@Override
	public Expression visitLeftHandSide( ChoralParser.LeftHandSideContext lhs ) {
		debugInfo();
		return ifPresent( lhs.expressionName() )
				.applyOrElse( this::visitExpressionName,
						() -> visitFieldAccess( lhs.fieldAccess() ) );
	}

	@Override
	public AssignExpression.Operator visitAssignmentOperator(
			ChoralParser.AssignmentOperatorContext ao
	) {
		debugInfo();
		return AssignExpression.Operator.getIfPresent( ao.getText() );
	}

	@Override
	public Expression visitExpression( ChoralParser.ExpressionContext e ) {
		debugInfo();
		return ifPresent( e.shortCircuitOrExpression() ).applyOrElse(
				this::visitShortCircuitOrExpression,
				() -> visitAssignment( e.assignment() )
		);
	}

	@Override
	public Expression visitShortCircuitOrExpression(
			ChoralParser.ShortCircuitOrExpressionContext soe
	) {
		debugInfo();
		if( isPresent( soe.shortCircuitOrExpression() ) ) {
			return new BinaryExpression(
					visitShortCircuitOrExpression( soe.shortCircuitOrExpression() ),
					visitShortCircuitAndExpression( soe.shortCircuitAndExpression() ),
					BinaryExpression.Operator.SHORT_CIRCUITED_OR,
					getPosition( soe )
			);
		} else {
			return visitShortCircuitAndExpression( soe.shortCircuitAndExpression() );
		}
	}

	@Override
	public Expression visitShortCircuitAndExpression(
			ChoralParser.ShortCircuitAndExpressionContext sae
	) {
		debugInfo();
		if( isPresent( sae.shortCircuitAndExpression() ) ) {
			return new BinaryExpression(
					visitShortCircuitAndExpression( sae.shortCircuitAndExpression() ),
					visitOrExpression( sae.orExpression() ),
					BinaryExpression.Operator.SHORT_CIRCUITED_AND,
					getPosition( sae )
			);
		} else {
			return visitOrExpression( sae.orExpression() );
		}
	}

	@Override
	public Expression visitOrExpression( ChoralParser.OrExpressionContext oe ) {
		debugInfo();
		if( isPresent( oe.orExpression() ) ) {
			return new BinaryExpression(
					visitOrExpression( oe.orExpression() ),
					visitAndExpression( oe.andExpression() ),
					BinaryExpression.Operator.OR,
					getPosition( oe )
			);
		} else {
			return visitAndExpression( oe.andExpression() );
		}
	}

	@Override
	public Expression visitAndExpression( ChoralParser.AndExpressionContext ae ) {
		debugInfo();
		if( isPresent( ae.andExpression() ) ) {
			return new BinaryExpression(
					visitAndExpression( ae.andExpression() ),
					visitEqualityExpression( ae.equalityExpression() ),
					BinaryExpression.Operator.AND,
					getPosition( ae )
			);
		} else {
			return visitEqualityExpression( ae.equalityExpression() );
		}
	}

	@Override
	public Expression visitEqualityExpression( ChoralParser.EqualityExpressionContext ee ) {
		debugInfo();
		if( isPresent( ee.equalityExpression() ) ) {
			return new BinaryExpression(
					visitEqualityExpression( ee.equalityExpression() ),
					visitRelationalExpression( ee.relationalExpression() ),
					BinaryExpression.Operator.getIfPresent( ee.op.getText() ),
					getPosition( ee )
			);
		} else {
			return visitRelationalExpression( ee.relationalExpression() );
		}
	}

	/* * * * * * * * * CONDITIONALS AND MATCHES * * * * * * * * * * * */

	@Override
	public Expression visitRelationalExpression( ChoralParser.RelationalExpressionContext re ) {
		debugInfo();
		if( isPresent( re.relationalExpression() ) ) {
			return new BinaryExpression(
					visitRelationalExpression( re.relationalExpression() ),
					visitAdditiveExpression( re.additiveExpression() ),
					BinaryExpression.Operator.getIfPresent( re.op.getText() ),
					getPosition( re )
			);
		} else {
			return visitAdditiveExpression( re.additiveExpression() );
		}
	}

	@Override
	public Expression visitAdditiveExpression( ChoralParser.AdditiveExpressionContext ae ) {
		debugInfo();
		if( isPresent( ae.additiveExpression() ) ) {
			return new BinaryExpression(
					visitAdditiveExpression( ae.additiveExpression() ),
					visitMultiplicativeExpression( ae.multiplicativeExpression() ),
					BinaryExpression.Operator.getIfPresent( ae.op.getText() ),
					getPosition( ae )
			);
		} else {
			return visitMultiplicativeExpression( ae.multiplicativeExpression() );
		}
	}

	@Override
	public Expression visitMultiplicativeExpression(
			ChoralParser.MultiplicativeExpressionContext me
	) {
		debugInfo();
		if( isPresent( me.multiplicativeExpression() ) ) {
			return new BinaryExpression(
					visitMultiplicativeExpression( me.multiplicativeExpression() ),
					visitUnaryExpression( me.unaryExpression() ),
					BinaryExpression.Operator.getIfPresent( me.op.getText() ),
					getPosition( me )
			);
		} else {
			return visitUnaryExpression( me.unaryExpression() );
		}
	}

	@Override
	public Expression visitUnaryExpression( ChoralParser.UnaryExpressionContext ue ) {
		debugInfo();
		if( isPresent( ue.primary() ) ) {
			return visitPrimary( ue.primary() );
		} else if( isPresent( ue.statementExpression() ) ) {
			return visitStatementExpression( ue.statementExpression() );
		} else {
			return new NotExpression( visitUnaryExpression( ue.unaryExpression() ),
					getPosition( ue ) );
		}
	}

	public Object visitFwd_chain( ChoralParser.Fwd_chainContext ctx ) {
		return null;
	}

	@Override
	public Expression visitPrimary( ChoralParser.PrimaryContext p ) {
		debugInfo();
		if( isPresent( p.literal() ) ) {
			return visitLiteral( p.literal(), visitWorldArgument( p.worldArgument() ) );
		} else if( isPresent( p.expression() ) ) {
			return new EnclosedExpression( visitExpression( p.expression() ), getPosition( p ) );
		}
		if( isPresent( p.NULL() ) ) {
			return new NullExpression( visitWorldArguments( p.worldArguments() ),
					getPosition( p ) );
		} else if( isPresent( p.THIS() ) ) {
			return new ThisExpression( getPosition( p ) );
		} else {
			return new SuperExpression( getPosition( p ) );
		}
	}

	@Override
	public IfStatement visitIfThenStatement( ChoralParser.IfThenStatementContext its ) {
		debugInfo();
		return new IfStatement( visitExpression( its.expression() ), visitBlock( its.block() ),
				new NilStatement( getPosition( its.getStop() ) ), null, getPosition( its ) );
	}

	@Override
	public IfStatement visitIfThenElseStatement( ChoralParser.IfThenElseStatementContext ites ) {
		debugInfo();
		return new IfStatement(
				visitExpression( ites.expression() ),
				visitBlock( ites.thenBlock ),
				visitBlock( ites.elseBlock ),
				null,
				getPosition( ites )
		);
	}

	/* * * * * * * * * NAMES AND LITERALS * * * * * * */

	public SwitchStatement visitSwitchStatement( ChoralParser.SwitchStatementContext ssc ) {
		debugInfo();
		return new SwitchStatement(
				visitExpression( ssc.expression() ),
				visitSwitchBlock( ssc.switchBlock() ),
				null,
				getPosition( ssc )
		);
	}

	public Map< SwitchArgument< ? >, Statement > visitSwitchBlock(
			ChoralParser.SwitchBlockContext sbc
	) {
		debugInfo();
		return sbc.switchCase().stream()
				.flatMap( c -> visitSwitchCase( c ).entrySet().stream() )
				.collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue )
				);
	}

	@Override
	public Map< SwitchArgument, Statement > visitSwitchCase( ChoralParser.SwitchCaseContext scc ) {
		debugInfo();
		Statement s = visitBlock( scc.block() );
		return isPresent( scc.switchArgs() ) ?
				visitSwitchArgs( scc.switchArgs() ).stream()
						.map( a -> new AbstractMap.SimpleEntry<>( a, s ) )
						.collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ) )
				: Collections.singletonMap(
				new SwitchArgument.SwitchArgumentDefault( getPosition( scc.getStop() ) ), s );
	}

	@Override
	public List< SwitchArgument > visitSwitchArgs( ChoralParser.SwitchArgsContext sac ) {
		debugInfo();
		List< SwitchArgument > l = new ArrayList<>();
		if( isPresent( sac.literal() ) ) {
			l.add( new SwitchArgument.SwitchArgumentLiteral(
					visitLiteral( sac.literal(), new WorldArgument(
							getName( sac.worldArgument().Identifier() ),
							getPosition( sac.worldArgument() )
					) ) ) );
		} else {
			l.add( new SwitchArgument.SwitchArgumentLabel( getName( sac.Identifier() ) ) );
		}
		ifPresent( sac.switchArgs() ).apply( s -> l.addAll( visitSwitchArgs( s ) ) );
		return l;
	}



	/* * * * * * * * * * * * GENERIC VISITS * * * * * * * */

	@Override
	public TryCatchStatement visitTryCatchStatement( ChoralParser.TryCatchStatementContext tcs ) {
		debugInfo();
		return new TryCatchStatement(
				visitBlock( tcs.block() ),
				tcs.catchBlock().stream().map( this::visitCatchBlock ).collect(
						Collectors.toList() ),
				null,
				getPosition( tcs )
		);
	}

	@Override
	public Pair< VariableDeclaration, Statement > visitCatchBlock(
			ChoralParser.CatchBlockContext cb
	) {
		debugInfo();
		return new Pair<>(
				new VariableDeclaration(
						getName( cb.formalParameter().Identifier() ),
						visitReferenceType( cb.formalParameter().referenceType() ),
						getPosition( cb )
				),
				visitBlock( cb.block() )
		);
	}

	@Override
	public Object visitLiteral( ChoralParser.LiteralContext ctx ) {
		throw new UnsupportedOperationException(
				"visitLiteral( ChoralParser.LiteralContext ctx ) should not be used. " +
						"Use visitLiteral( ChoralParser.LiteralContext l, WorldArgument w ) instead"
		);
	}

	public LiteralExpression visitLiteral( ChoralParser.LiteralContext l, WorldArgument w ) {
		if( isPresent( l.BooleanLiteral() ) ) {
			return new LiteralExpression.BooleanLiteralExpression(
					Boolean.parseBoolean( l.BooleanLiteral().getText() ), w, getPosition( l ) );
		} else if( isPresent( l.IntegerLiteral() ) ) {
			return new LiteralExpression.IntegerLiteralExpression(
					Integer.parseInt( l.IntegerLiteral().getText() ), w, getPosition( l ) );
		} else if( isPresent( l.FloatingPointLiteral() ) ) {
			return new LiteralExpression.DoubleLiteralExpression(
					Double.parseDouble( l.FloatingPointLiteral().getText() ), w, getPosition( l ) );
		} else if( isPresent( l.StringLiteral() ) ) {
			return new LiteralExpression.StringLiteralExpression( l.StringLiteral().getText(), w,
					getPosition( l ) );
		} else {
			throw new SyntaxException( getPosition( l ),
					"Unrecognized literal: '" + l.getText() + "'" );
		}
	}

	@Override
	public Expression visitExpressionName( ChoralParser.ExpressionNameContext en ) {
		debugInfo();
		FieldAccessExpression fa = new FieldAccessExpression( getName( en.Identifier() ),
				getPosition( en ) );
		return ifPresent( en.ambiguousName() ).applyOrElse(
				( an ) -> new ScopedExpression( visitAmbiguousName( an ), fa, getPosition( en ) ),
				() -> fa );
	}

	/* * * * * * * * * * * UTILS * * * * * * * * * */

	@Override
	public Expression visitAmbiguousName( ChoralParser.AmbiguousNameContext an ) {
		debugInfo();
		FieldAccessExpression fa = new FieldAccessExpression( getName( an.Identifier() ),
				getPosition( an ) );
		return ifPresent( an.ambiguousName() ).applyOrElse(
				( _an ) -> new ScopedExpression( visitAmbiguousName( _an ), fa, getPosition( an ) ),
				() -> fa );
	}

	@Override
	public Node visit( ParseTree parseTree ) {
		throw new UnsupportedOperationException(
				"The AstOptimizer should not visit the ParseTree" );
	}

	@Override
	public Object visitChildren( RuleNode ruleNode ) {
		throw new UnsupportedOperationException(
				"The AstOptimizer should not visit nodes as generic RuleNodes" );
	}

	@Override
	public Object visitTerminal( TerminalNode terminalNode ) {
		throw new UnsupportedOperationException(
				"The AstOptimizer should not visit TerminalNodes" );
	}

	@Override
	public Object visitErrorNode( ErrorNode errorNode ) {
		new ParseException( "Parsing Error " + errorNode.getText(),
				errorNode.getSourceInterval().a ).printStackTrace();
		debugInfo();
		return null;
	}

	public AstOptimizer excludeDebugMethod( String m ) {
		debugExcludeMethods.add( m );
		return this;
	}

	private Name getName( TerminalNode t ) {
		return new Name( t.getText(), getPosition( t.getSymbol() ) );
	}

	private Position getPosition( Token t ) {
		return new Position( this.file, t.getLine(), t.getCharPositionInLine() );
	}

	private Position getPosition( TerminalNode c ) {
		return getPosition( c.getSymbol() );
	}

	private Position getPosition( ParserRuleContext c ) {
		return getPosition( c.getStart() );
	}

	private void setLastMethod( String m ) {
		lastMethod = m;
		cmCounter = 1;
	}

	private void debugInfo() {
		if( debug ) {
			String currentMethod = Thread.currentThread().getStackTrace()[ 2 ].getMethodName();
			if( lastMethod.equals( currentMethod ) ) {
				cmCounter++;
			} else {
				if( !debugExcludeMethods.contains( lastMethod ) ) {
					System.out.println(
							lastMethod + ( ( cmCounter > 1 ) ? ( " x " + cmCounter ) : "" ) );
				}
				setLastMethod( currentMethod );
			}
		}
	}

	private void flushDebug() {
		excludeDebugMethod( "flushDebug" );
		debugInfo();
	}

	private boolean isPresent( ParserRuleContext p ) {
		return p != null && !p.isEmpty();
	}

	private boolean isPresent( TerminalNode p ) {
		return p != null;
	}

	private < T > IfPresent< T > ifPresent( T o ) {
		return new IfPresent<>( o );
	}

	private static class IfPresent< T > {

		private final Optional< T > o;

		IfPresent( T o ) {
			this.o = Optional.ofNullable( o );
		}

		< R > Optional< R > apply( Function< T, R > f ) {
			if( f == null ) {
				throw new RuntimeException( "Application function must be not null" );
			}
			return o.map( f );
		}

		< R > R applyOrElse( Function< T, R > f, Supplier< R > e ) {
			if( f == null || e == null ) {
				throw new RuntimeException(
						"Both application and alternative functions must be not null" );
			}
			return o.map( f ).orElseGet( e );
		}

	}

}
