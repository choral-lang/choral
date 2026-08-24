package choral.compiler.typer.scope;

import choral.ast.Name;
import choral.exceptions.AstPositionedException;
import choral.types.GroundClass;
import choral.types.GroundClassOrInterface;
import choral.types.GroundDataType;
import choral.types.GroundInterface;
import choral.types.HigherClassOrInterface;
import choral.utils.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Interface for scopes that declare and look up local variables (method bodies and blocks). Also
 * provides {@link #lookupThis()}, {@link #lookupSuper()}, and {@link #getChannels()} — which
 * enumerates all channel-typed variables and fields visible in the current scope chain, used by
 * communication inference.
 */
public interface VariableDeclarationScope extends Scope {

	Optional< ? extends GroundDataType > lookupVariable( String identifier );

	default GroundDataType assertLookupVariable( String identifier ) {
		return lookupVariable( identifier ).orElseThrow(
				() -> new UnresolvedSymbolException( identifier ) );
	}

	default GroundDataType assertLookupVariable( Name query ) {
		return lookupVariable( query.identifier() ).orElseThrow(
				() -> new AstPositionedException( query.position(),
						new UnresolvedSymbolException( query.identifier() ) ) );
	}

	void declareVariable( String identifier, GroundDataType type );

	Optional< ? extends GroundDataType > lookupVariableOrField( String identifier );

	default GroundDataType assertLookupVariableOrField( String identifier ) {
		return lookupVariableOrField( identifier ).orElseThrow(
				() -> new UnresolvedSymbolException( identifier ) );
	}

	default GroundDataType assertLookupVariableOrField( Name query ) {
		return lookupVariableOrField( query.identifier() ).orElseThrow(
				() -> new AstPositionedException( query.position(),
						new UnresolvedSymbolException( query.identifier() ) ) );
	}

	GroundClassOrInterface lookupThis();

	GroundClass lookupSuper();

	Scope parent();

	Map< String, GroundDataType > variables();

	BlockScope newBlockScope();

	/**
	 * Collects channels available in the scope by looking at the fields of "this"
	 * and the enclosing method's arguments
	 */
	default List< Pair< String, GroundInterface > > getChannels() {
		Map< String, GroundInterface > channels = new HashMap<>();
		HigherClassOrInterface diDataChannel = assertLookupClassOrInterface(
				"choral.channels.DiDataChannel" );
		HigherClassOrInterface diSelectChannel = assertLookupClassOrInterface(
				"choral.channels.DiSelectChannel" );

		Predicate< GroundInterface > isChannel = ( type ) ->
				type.allExtendedInterfaces().anyMatch( parent -> {
					var innerType = parent.typeConstructor().innerType();
					return diDataChannel.innerType().isSubtypeOf( innerType ) ||
							diSelectChannel.innerType().isSubtypeOf( innerType );
				} );

		VariableDeclarationScope currentScope = this;
		while( true ) {
			currentScope.variables().forEach( ( key, val ) -> {
				if( val instanceof GroundInterface type ) {
					if( isChannel.test( type ) ) {
						channels.putIfAbsent( key, type );
					}
				}
			} );

			if( parent() instanceof VariableDeclarationScope parent ) {
				currentScope = parent;
			} else {
				break;
			}
		}

		lookupThis().fields().forEach( field -> {
			if( field.type() instanceof GroundInterface type ) {
				if( isChannel.test( type ) ) {
					channels.putIfAbsent( field.identifier(), type );
				}
			}
		} );

		return channels.entrySet().stream()
				.map( entry -> new Pair<>( entry.getKey(), entry.getValue() ) )
				.toList();
	}

}
