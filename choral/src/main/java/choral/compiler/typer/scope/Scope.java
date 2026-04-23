package choral.compiler.typer.scope;

import choral.ast.Name;
import choral.types.*;

import java.util.Optional;

public interface Scope {

	Optional< ? extends HigherDataType > lookupDataType( String query );

	HigherDataType assertLookupDataType( String query );

	HigherDataType assertLookupDataType( Name query );

	Optional< ? extends HigherReferenceType > lookupReferenceType( String query );

	HigherReferenceType assertLookupReferenceType( String query );

	HigherReferenceType assertLookupReferenceType( Name query );

	Optional< ? extends HigherClassOrInterface > lookupClassOrInterface( String query );

	HigherClassOrInterface assertLookupClassOrInterface( String query );

	HigherClassOrInterface assertLookupClassOrInterface( Name query );

	Optional< ? extends HigherTypeParameter > lookupTypeParameter( String query );

	HigherTypeParameter assertLookupTypeParameter( String query );

	HigherTypeParameter assertLookupTypeParameter( Name query );

	Optional< ? extends World > lookupWorldParameter( String query );

	World assertLookupWorldParameter( String query );

	World assertLookupWorldParameter( Name query );

}
