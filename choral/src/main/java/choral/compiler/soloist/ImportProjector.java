package choral.compiler.soloist;

import choral.ast.CompilationUnit;
import choral.ast.ImportDeclaration;
import choral.ast.Name;
import choral.ast.body.Class;
import choral.ast.body.Enum;
import choral.ast.body.*;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.ChoralVisitorInterface;
import choral.compiler.unitNormaliser.UnitRepresentation;
import choral.types.HigherDataType;
import choral.types.World;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ImportProjector implements ChoralVisitorInterface< Void > {

	private final List< ImportDeclaration > imports;
	private final Set< ImportDeclaration > usedImports;

	public ImportProjector( List< ImportDeclaration > imports ) {
		this.imports = projectAllImports( imports );
		usedImports = new HashSet<>();
	}

	private List< ImportDeclaration > projectAllImports( List< ImportDeclaration > cImports ) {
		List< ImportDeclaration > pImports = new ArrayList<>();
		for( ImportDeclaration i : cImports ) {
			if( i.typeAnnotation().isPresent() && i.typeAnnotation().get().worldParameters().size() > 1 ) {
				for( World worldParameter : i.typeAnnotation().get().worldParameters() ) {
					pImports.add(
							new ImportDeclaration( i.name() + "_" + worldParameter.identifier(),
									i.position() ) );
				}
			} else {
				pImports.add( i );
			}
		}
		return pImports;
	}

	public List< ImportDeclaration > projectImports( Interface n ) {
		visit( n );
		return List.copyOf( usedImports );
	}

	public List< ImportDeclaration > projectImports( Class n ) {
		visit( n );
		return List.copyOf( usedImports );
	}

	public List< ImportDeclaration > projectImports( Enum n ) {
		visit( n );
		return List.copyOf( usedImports );
	}

	@Override
	public Void visit( CompilationUnit n ) {
		throw new SoloistProjectorException(
				"Import projection should not be launched on Compilation Units" );
	}

	@Override
	public Void visit( ImportDeclaration n ) {
		throw new SoloistProjectorException(
				"Import projection should not be launched on import declarations" );
	}

	@Override
	public Void visit( Class n ) {
		n.implementsInterfaces().forEach( this::visit );
		n.typeParameters().forEach( this::visit );
		ifPresent( n.extendsClass() ).apply( this::visit );
		n.fields().forEach( this::visit );
		n.methods().forEach( this::visit );
		n.annotations().forEach( this::visit );
		n.constructors().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( Enum n ) {
		n.annotations().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( Interface n ) {
		n.typeParameters().forEach( this::visit );
		n.extendsInterfaces().forEach( this::visit );
		n.methods().forEach( this::visit );
		n.annotations().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( Statement n ) {
		n.accept( this );
		if( n.continuation() != null ) {
			visit( n.continuation() );
		}
		return null;
	}

	@Override
	public Void visit( BlockStatement n ) {
		visit( n.enclosedStatement() );
		return null;
	}

	@Override
	public Void visit( SelectStatement n ) {
		visit( n.channelExpression() );
		visit( n.enumConstructor() );
		return null;
	}

	@Override
	public Void visit( ScopedExpression n ) {
		visit( n.scope() );
		visit( n.scopedExpression() );
		return null;
	}

	@Override
	public Void visit( ExpressionStatement n ) {
		visit( n.expression() );
		return null;
	}

	@Override
	public Void visit( IfStatement n ) {
		visit( n.condition() );
		visit( n.ifBranch() );
		visit( n.elseBranch() );
		return null;
	}

	@Override
	public Void visit( SwitchStatement n ) {
		visit( n.guard() );
		n.cases().forEach( ( c, s ) -> visit( s ) );
		return null;
	}

	@Override
	public Void visit( TryCatchStatement n ) {
		visit( n.body() );
		n.catches().forEach( p -> {
			visit( p.left() );
			visit( p.right() );
		} );
		return null;
	}

	@Override
	public Void visit( NilStatement n ) {
		return null;
	}

	@Override
	public Void visit( ReturnStatement n ) {
		visit( n.returnExpression() );
		return null;
	}

	@Override
	public Void visit( Expression n ) {
		n.accept( this );
		return null;
	}

	@Override
	public Void visit( AssignExpression n ) {
		visit( n.target() );
		visit( n.value() );
		return null;
	}

	@Override
	public Void visit( BinaryExpression n ) {
		visit( n.left() );
		visit( n.right() );
		return null;
	}

	@Override
	public Void visit( ClassInstantiationExpression n ) {
		visit( n.typeExpression() );
		n.arguments().forEach( this::visit );
		n.typeArguments().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( EnumCaseInstantiationExpression n ) {
		return null;
	}

	@Override
	public Void visit( EnclosedExpression n ) {
		visit( n.nestedExpression() );
		return null;
	}

	@Override
	public Void visit( FieldAccessExpression n ) {
		return null;
	}

	@Override
	public Void visit( StaticAccessExpression n ) {
		visit( n.typeExpression() );
		return null;
	}

	@Override
	public Void visit( MethodCallExpression n ) {
		n.arguments().forEach( this::visit );
		n.typeArguments().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( Name n ) {
		return null;
	}

	@Override
	public Void visit( NotExpression n ) {
		visit( n.expression() );
		return null;
	}

	@Override
	public Void visit( ThisExpression n ) {
		return null;
	}

	@Override
	public Void visit( SuperExpression n ) {
		return null;
	}

	@Override
	public Void visit( NullExpression n ) {
		return null;
	}

	@Override
	public Void visit( VariableDeclarationStatement n ) {
		n.variables().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( BlankExpression n ) {
		return null;
	}

	@Override
	public Void visit( LiteralExpression.BooleanLiteralExpression n ) {
		return null;
	}

	@Override
	public Void visit( LiteralExpression.DoubleLiteralExpression n ) {
		return null;
	}

	@Override
	public Void visit( LiteralExpression.IntegerLiteralExpression n ) {
		return null;
	}

	@Override
	public Void visit( LiteralExpression.StringLiteralExpression n ) {
		return null;
	}

	@Override
	public Void visit( SwitchArgument< ? > n ) {
		return null;
	}

	@Override
	public Void visit( CaseSignature n ) {
		return null;
	}

	@Override
	public Void visit( Field n ) {
		visit( n.typeExpression() );
		return null;
	}

	@Override
	public Void visit( FormalMethodParameter n ) {
		visit( n.type() );
		return null;
	}

	@Override
	public Void visit( ClassMethodDefinition n ) {
		visit( n.signature() );
		n.annotations().forEach( this::visit );
		n.body().ifPresent( this::visit );
		return null;
	}

	@Override
	public Void visit( InterfaceMethodDefinition n ) {
		visit( n.signature() );
		n.annotations().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( MethodSignature n ) {
		visit( n.returnType() );
		n.parameters().forEach( this::visit );
		n.typeParameters().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( ConstructorDefinition n ) {
		visit( n.signature() );
		n.annotations().forEach( this::visit );
		visit( n.body() );
		return null;
	}

	@Override
	public Void visit( ConstructorSignature n ) {
		n.typeParameters().forEach( this::visit );
		n.parameters().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( VariableDeclaration n ) {
		visit( n.type() );
		n.initializer().ifPresent( this::visit );
		return null;
	}

	@Override
	public Void visit( TypeExpression n ) {
		if( n.typeAnnotation().isPresent() && n.typeAnnotation().get().isHigherType() ) {
			HigherDataType dataType = (HigherDataType) n.typeAnnotation().get();
			String qualifiedName = n.name().identifier();
			addImport( qualifiedName );
		} else if( n.name().equals( UnitRepresentation.UNIT ) ) {
			addImport( UnitRepresentation.UNIT_IMPORT_DECLARATION.name() );
		} else {
			String qualifiedName = n.name().identifier();
			addImport( qualifiedName );
		}
		n.typeArguments().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( WorldArgument n ) {
		return null;
	}

	@Override
	public Void visit( FormalTypeParameter n ) {
		n.upperBound().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( FormalWorldParameter n ) {
		return null;
	}

	@Override
	public Void visit( Annotation n ) {
		addImport( n.getName().identifier() );
		return null;
	}

	private void addImport( String name ) {
		usedImports.addAll(
				imports.stream()
						.filter( i ->
								i.name().endsWith( "*" )
										|| ( i.name().contains( "." )
										&& i.name().substring(
										i.name().lastIndexOf( "." ) + 1 ).equals( name ) )
										|| i.name().equals( name )
						).collect( Collectors.toSet() )
		);
		imports.removeAll( usedImports );
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
