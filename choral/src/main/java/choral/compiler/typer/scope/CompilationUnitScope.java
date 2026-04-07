package choral.compiler.typer.scope;

import choral.ast.ImportDeclaration;
import choral.exceptions.AstPositionedException;
import choral.exceptions.StaticVerificationException;
import choral.types.*;
import choral.types.Package;
import choral.utils.Formatting;

import java.util.*;
import java.util.stream.Collectors;

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
	private final choral.types.Package declarationPackage;
	private final Map< HigherClassOrInterface, ClassOrInterfaceStaticScope > templateScopes = new HashMap<>();
	private boolean pendingSingleImports = true;
	private boolean pendingOnDemandImports = true;

	public CompilationUnitScope(
			choral.types.Package declarationPackage, List< ImportDeclaration > declaredImports
	) {
		super();
		this.declarationPackage = declarationPackage;
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
				while( i < path.length - 1 /* last one is always ".*" */ ) {
					// declaredPackage() returns full path up till passed package / class.
					// so passing "io" from "java.io", will return "java.io"
					Optional< choral.types.Package > x = pkg.declaredPackage( path[ i ] );
					if( x.isPresent() ) {
						pkg = x.get();
					} else {
						throw new AstPositionedException( ip.position(),
								new StaticVerificationException(
										"cannot resolve symbol '" + path[ i ] + "'" ) );
					}
					i += 1;
				}
				onDemandImports.add( pkg );
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
		Optional< ? extends HigherClassOrInterface > result;
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
			} else {
				result = Optional.empty();
			}
			// call classlifter with "declarationPackage.universe().rootPackage()"
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
						.map( x -> x.declaredType( query ) ).filter(
								Optional::isPresent ).map(
								Optional::get )
						.filter( this::hasPublicAccess ).collect( Collectors.toList() );
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
