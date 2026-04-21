package choral.compiler.typer.scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import choral.ast.ImportDeclaration;
import choral.compiler.typer.ClassLifter;
import choral.exceptions.AstPositionedException;
import choral.exceptions.StaticVerificationException;
import choral.types.HigherClassOrInterface;
import choral.types.HigherDataType;
import choral.types.HigherReferenceType;
import choral.types.HigherTypeParameter;
import choral.types.Package;
import choral.types.World;
import choral.utils.Formatting;

/**
 * Top-level scope for a single Choral source file. Resolves type names by searching the current
 * package, single-type imports, then on-demand imports — with {@code java.lang} and
 * {@code choral.lang} always included implicitly.
 */
public final class CompilationUnitScope extends BaseScope {

	private final static String[] defaultOnDemandImports = new String[] { "java.lang", "choral.lang" };
	private final List< HigherClassOrInterface > singleImports;
	private final List< ImportDeclaration > singleImportStatements;
	private final List< Package > onDemandImports;
	private final List< ImportDeclaration > onDemandImportStatements;
	private final Set<String> javaOnDemandImportStatements;
	private final List<String> specialTypes = new ArrayList<>(Arrays.asList("Object", "Enum", "String", "Exception",
		"Number", "Boolean", "Byte", "Character", "Short", "Integer", "Long", "Float", "Double"));
	private final choral.types.Package declarationPackage;
	private final Map< HigherClassOrInterface, ClassOrInterfaceStaticScope > templateScopes = new HashMap<>();
	private boolean pendingSingleImports = true;
	private boolean pendingOnDemandImports = true;
	private final ClassLifter classLifter;

	public CompilationUnitScope(
			choral.types.Package declarationPackage, List< ImportDeclaration > declaredImports,
			ClassLifter classLifter
	) {
		super();
		this.declarationPackage = declarationPackage;
		this.classLifter = classLifter;
		singleImportStatements = new ArrayList<>( declaredImports.size() );
		onDemandImportStatements = new ArrayList<>( declaredImports.size() );
		for( ImportDeclaration ip : declaredImports ) {
			if( ip.isOnDemand() ) {
				onDemandImportStatements.add( ip );
			} else {
				singleImportStatements.add( ip );
			}
		}
		singleImports = new ArrayList<>( singleImportStatements.size() );
		onDemandImports = new ArrayList<>(
				onDemandImportStatements.size() + defaultOnDemandImports.length );
		javaOnDemandImportStatements = new HashSet<>(onDemandImportStatements.size() + 1);
		choral.types.Package root = declarationPackage.universe().rootPackage();
		for( String defaultOnDemandImport : defaultOnDemandImports ) {
			onDemandImports.add( root.declarePackage( defaultOnDemandImport ) );
		}
	}

	private void resolveSingleImports() {
		if( pendingSingleImports ) {
			for( ImportDeclaration ip : singleImportStatements ) {
				choral.types.Package pkg = declarationPackage.universe().rootPackage();
				Optional< ? extends HigherClassOrInterface > type = pkg.declaredType( ip.name() );
				if( type.isPresent() ) {
					assertPublicAccess( type.get() );
					singleImports.add( type.get() );
				} else { // class lifter here?
					throw new AstPositionedException( ip.position(),
							new StaticVerificationException(
									"cannot resolve symbol '" + ip.name() + "'" ) );
				}
			}
			pendingSingleImports = false;
		}
	}

	private void resolveOnDemandImports() {
		if( pendingOnDemandImports ) {
			for( ImportDeclaration ip : onDemandImportStatements ) {
				choral.types.Package pkg = declarationPackage.universe().rootPackage();
				String[] path = ip.name().split( "\\." );
				int i = 0;
				boolean javaPackage = false;
				while( i < path.length - 1 /* last one is always "*" */ ) {
					// declaredPackage() returns full path up till passed package / class.
					// so passing "io" from "java.io", will return "java.io"
					Optional< choral.types.Package > x = pkg.declaredPackage( path[ i ] );
					if( x.isPresent() ) { 
						pkg = x.get();
					} else { // We assume package to be java package, save for later
						javaPackage = true;
						break;
					}
					i += 1;
				}
				if(!javaPackage){
					onDemandImports.add( pkg );
				} else {
					// 'ip.name().length() - 2' is to cut off the '.*' segment 
					javaOnDemandImportStatements.add(ip.name().substring(0, ip.name().length() - 2));
				}
			}
			pendingOnDemandImports = false;
		}
	}

	@Override
	public Optional< ? extends HigherDataType > lookupDataType( String query ) {
		Optional< ? extends HigherDataType > result = lookupClassOrInterface( query );
		if( result.isEmpty() ) {
			result = declarationPackage.universe().primitiveDataType( query );
		}
		return result;
	}

	@Override
	public Optional< ? extends HigherReferenceType > lookupReferenceType( String query ) {
		return lookupClassOrInterface( query );
	}

	@Override
	public Optional< ? extends HigherClassOrInterface > lookupClassOrInterface( String query ) {
		String[] path = query.split( "\\." );
		Optional< ? extends HigherClassOrInterface > result = Optional.empty();
		if( path.length > 1 ) {
			// fully qualified name (only because Choral does not have nested classes yet)
			Optional< choral.types.Package > pkg = Optional.of(
					declarationPackage.universe().rootPackage() );
			int i = 0;
			while( pkg.isPresent() && i < path.length - 1 ) {
				pkg = pkg.get().declaredPackage( path[ i ] );
				i += 1;
			}
			if( pkg.isPresent() && i == path.length - 1 ) {
				Package p = pkg.get();
				result = p.declaredType( path[ i ] );
			} 
			if(result.isEmpty()){
				// System.out.println("Classlifter query: " + query);
				result = classLifter.liftClassOrInterface(query);
			}
		} else {
			// search current package
			result = declarationPackage.declaredType( query );
			// search imports
			if( result.isEmpty() ) {
				resolveSingleImports();
				for( HigherClassOrInterface type : singleImports ) {
					if( type.identifier().equals( query ) ) {
						result = Optional.of( type );
						break;
					}
				}
			}
			// search delayed imports
			if( result.isEmpty() ) {
				resolveOnDemandImports();
				List< HigherClassOrInterface > results = onDemandImports.stream()
						.map( x -> x.declaredType( query ) )
						.filter(Optional::isPresent )
						.map(Optional::get )
						.filter( this::hasPublicAccess )
						.collect( Collectors.toList() );
				
				List<HigherClassOrInterface> liftedResults = new ArrayList<>();

				if(!specialTypes.contains(query)){
					Stream<String> javaPackages = results.isEmpty()
						? Stream.concat(javaOnDemandImportStatements.stream(), Stream.of("java.lang")).distinct()
						: javaOnDemandImportStatements.stream(); 
					
					liftedResults = javaPackages
						.map(javaPackage -> lookupClassOrInterface(javaPackage + "." +  query))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.filter(this::hasPublicAccess)
						.collect(Collectors.toList());
				}

				results.addAll(liftedResults);
				if( results.size() == 0 ) {
					result = Optional.empty();
				} else if( results.size() == 1 ) {
					result = Optional.of( results.get( 0 ) );
				} else {
					throw new StaticVerificationException(
						"reference to '" + query + "' is ambiguous, " +
						results.stream().map(
										x -> "'" + x.identifier( true ) + "'" )
								.collect( Collectors.collectingAndThen(
										Collectors.toList(),
										Formatting.joiningOxfordComma() ) ) +
						" are ambiguous"
					);
				}
			}
		}
		result.ifPresent( this::assertPublicAccess );
		return result;
	}

	public Optional< ? extends HigherTypeParameter > lookupTypeParameter( String query ) {
		return Optional.empty();
	}

	public Optional< World > lookupWorldParameter( String query ) {
		return Optional.empty();
	}

	public ClassOrInterfaceStaticScope getScope( HigherClassOrInterface type ) {
		ClassOrInterfaceStaticScope scope = templateScopes.get( type );
		if( scope == null ) {
			scope = new ClassOrInterfaceStaticScope( this, type );
			templateScopes.put( type, scope );
		}
		return scope;
	}

	private boolean hasPublicAccess( HigherClassOrInterface type ) {
		return type.declarationContext() == declarationPackage || type.isPublic();
	}

	private void assertPublicAccess( HigherClassOrInterface type ) {
		if( !hasPublicAccess( type ) ) {
			throw new StaticVerificationException( type.variety().labelSingular
					+ " '"
					+ type.identifier( true )
					+ "' has not public access" );
		}
	}
}
