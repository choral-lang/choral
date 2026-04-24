package choral.compiler.typer.scope;

import choral.ast.Name;
import choral.exceptions.AstPositionedException;
import choral.types.*;

/**
 * Provides {@code assertLookup*} methods that throw an exception if the corresponding {@code
 * lookup*} fails.
 */
public abstract class BaseScope implements Scope {

  @Override
  public final HigherDataType assertLookupDataType(String query) {
    return lookupDataType(query).orElseThrow(() -> new UnresolvedSymbolException(query));
  }

  @Override
  public final HigherDataType assertLookupDataType(Name query) {
    return lookupDataType(query.identifier())
        .orElseThrow(
            () ->
                new AstPositionedException(
                    query.position(), new UnresolvedSymbolException(query.identifier())));
  }

  @Override
  public final HigherReferenceType assertLookupReferenceType(String query) {
    return lookupReferenceType(query).orElseThrow(() -> new UnresolvedSymbolException(query));
  }

  @Override
  public final HigherReferenceType assertLookupReferenceType(Name query) {
    return lookupReferenceType(query.identifier())
        .orElseThrow(
            () ->
                new AstPositionedException(
                    query.position(), new UnresolvedSymbolException(query.identifier())));
  }

  @Override
  public final HigherClassOrInterface assertLookupClassOrInterface(String query) {
    return lookupClassOrInterface(query).orElseThrow(() -> new UnresolvedSymbolException(query));
  }

  @Override
  public final HigherClassOrInterface assertLookupClassOrInterface(Name query) {
    return lookupClassOrInterface(query.identifier())
        .orElseThrow(
            () ->
                new AstPositionedException(
                    query.position(), new UnresolvedSymbolException(query.identifier())));
  }

  @Override
  public final HigherTypeParameter assertLookupTypeParameter(String query) {
    return lookupTypeParameter(query).orElseThrow(() -> new UnresolvedSymbolException(query));
  }

  @Override
  public final HigherTypeParameter assertLookupTypeParameter(Name query) {
    return lookupTypeParameter(query.identifier())
        .orElseThrow(
            () ->
                new AstPositionedException(
                    query.position(), new UnresolvedSymbolException(query.identifier())));
  }

  @Override
  public final World assertLookupWorldParameter(String query) {
    return lookupWorldParameter(query).orElseThrow(() -> new UnresolvedSymbolException(query));
  }

  @Override
  public final World assertLookupWorldParameter(Name query) {
    return lookupWorldParameter(query.identifier())
        .orElseThrow(
            () ->
                new AstPositionedException(
                    query.position(), new UnresolvedSymbolException(query.identifier())));
  }
}
