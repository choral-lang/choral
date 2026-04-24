package choral.compiler.typer.scope;

import choral.exceptions.StaticVerificationException;
import choral.types.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Scope for the parameters and local variables in a method or constructor body. Its parent is
 * always a {@link CallableScope}, which is the scope for the callable's type parameters.
 */
public final class CallableBodyScope extends ChildScope implements VariableDeclarationScope {

  private final CallableScope parent;
  private final Map<String, GroundDataType> variables = new HashMap<>();

  public CallableBodyScope(CallableScope parent) {
    this.parent = parent;
    for (Signature.Parameter p : parent.callable.innerCallable().signature().parameters()) {
      variables.put(p.identifier(), p.type());
    }
  }

  @Override
  public CallableScope parent() {
    return parent;
  }

  @Override
  public Map<String, GroundDataType> variables() {
    return variables;
  }

  @Override
  public void declareVariable(String identifier, GroundDataType type) {
    if (lookupVariable(identifier).isEmpty()) {
      variables.put(identifier, type);
    } else {
      throw new StaticVerificationException(
          "variable '" + identifier + "' already defined in the scope");
    }
  }

  @Override
  public Optional<? extends GroundDataType> lookupVariable(String identifier) {
    return Optional.ofNullable(variables.get(identifier));
  }

  @Override
  public Optional<? extends GroundDataType> lookupVariableOrField(String identifier) {
    GroundDataType result = variables.get(identifier);
    if (result == null) {
      return parent().callable.declarationContext().field(identifier).map(Member.Field::type);
    } else {
      return Optional.of(result);
    }
  }

  @Override
  public GroundClassOrInterface lookupThis() {
    return parent.callable.declarationContext();
  }

  @Override
  public GroundClass lookupSuper() {
    if (lookupThis() instanceof GroundClass c) {
      return c.extendedClass().orElseThrow(() -> new UnresolvedSymbolException("super"));
    } else {
      throw new UnresolvedSymbolException("super");
    }
  }

  public BlockScope newBlockScope() {
    return new BlockScope(this);
  }
}
