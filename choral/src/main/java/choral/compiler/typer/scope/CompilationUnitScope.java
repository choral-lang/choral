package choral.compiler.typer.scope;

import choral.ast.ImportDeclaration;
import choral.compiler.typer.ClassLifter;
import choral.exceptions.AstPositionedException;
import choral.exceptions.StaticVerificationException;
import choral.types.*;
import choral.utils.Formatting;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Top-level scope for a single Choral source file. Resolves type names by searching the current
 * package, single-type imports, then on-demand imports — with {@code java.lang} and {@code
 * choral.lang} always included implicitly.
 */
public final class CompilationUnitScope extends BaseScope {

  private static final String[] defaultOnDemandImports =
      new String[] {"java.lang.*", "choral.lang.*"};
  private final List<ImportDeclaration> onDemandImports;

  private final List<ImportDeclaration> singleImportStatements;
  private final List<HigherClassOrInterface> singleImports;
  private boolean pendingSingleImports = true;

  private final choral.types.Package declarationPackage;
  private final Map<HigherClassOrInterface, ClassOrInterfaceStaticScope> templateScopes =
      new HashMap<>();
  private final ClassLifter classLifter;

  public CompilationUnitScope(
      choral.types.Package declarationPackage,
      List<ImportDeclaration> declaredImports,
      ClassLifter classLifter) {
    super();
    this.declarationPackage = declarationPackage;
    this.classLifter = classLifter;
    singleImportStatements = new ArrayList<>(declaredImports.size());
    onDemandImports = new ArrayList<>(declaredImports.size());
    HashSet<String> seenImports = new HashSet<>(declaredImports.size());
    for (ImportDeclaration ip : declaredImports) {
      if (ip.isOnDemand()) {
        if (seenImports.add(ip.name())) onDemandImports.add(ip);
      } else {
        singleImportStatements.add(ip);
      }
    }
    for (String defaultImport : defaultOnDemandImports) {
      if (seenImports.add(defaultImport)) {
        ImportDeclaration ip = new ImportDeclaration(defaultImport, null);
        onDemandImports.add(ip);
      }
    }
    singleImports = new ArrayList<>(singleImportStatements.size());
  }

  private void resolveSingleImports() {
    if (pendingSingleImports) {
      for (ImportDeclaration ip : singleImportStatements) {
        Optional<? extends HigherClassOrInterface> type =
            classLifter.lookup(ip.name(), ip.position());
        if (type.isPresent()) {
          assertPublicAccess(type.get());
          singleImports.add(type.get());
        } else {
          throw new AstPositionedException(
              ip.position(),
              new StaticVerificationException("cannot resolve symbol '" + ip.name() + "'"));
        }
      }
      pendingSingleImports = false;
    }
  }

  @Override
  public Optional<? extends HigherDataType> lookupDataType(String query) {
    var result = declarationPackage.universe().primitiveDataType(query);
    if (result.isPresent()) return result;
    return lookupClassOrInterface(query);
  }

  @Override
  public Optional<? extends HigherReferenceType> lookupReferenceType(String query) {
    return lookupClassOrInterface(query);
  }

  @Override
  public Optional<? extends HigherClassOrInterface> lookupClassOrInterface(String query) {
    String[] path = query.split("\\.");
    Optional<? extends HigherClassOrInterface> result = Optional.empty();
    if (path.length > 1) {
      result = classLifter.lookup(query, null);
    } else {
      // search current package
      result = declarationPackage.declaredType(query);
      // search imports
      if (result.isEmpty()) {
        resolveSingleImports();
        for (HigherClassOrInterface type : singleImports) {
          if (type.identifier().equals(query)) {
            result = Optional.of(type);
            break;
          }
        }
      }
      // search delayed imports
      if (result.isEmpty()) {
        List<HigherClassOrInterface> results =
            onDemandImports.stream()
                .map(
                    ip -> {
                      // 'ip.name().length() - 2' is to cut off the '.*' segment
                      String prefix = ip.name().substring(0, ip.name().length() - 2);
                      return classLifter.lookup(prefix + "." + query, ip.position());
                    })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(this::hasPublicAccess)
                .toList();

        if (results.isEmpty()) {
          result = Optional.empty();
        } else if (results.size() == 1) {
          result = Optional.of(results.get(0));
        } else {
          throw new StaticVerificationException(
              "reference to '"
                  + query
                  + "' is ambiguous, "
                  + results.stream()
                      .map(x -> "'" + x.identifier(true) + "'")
                      .collect(
                          Collectors.collectingAndThen(
                              Collectors.toList(), Formatting.joiningOxfordComma()))
                  + " are ambiguous");
        }
      }
    }
    result.ifPresent(this::assertPublicAccess);
    return result;
  }

  public Optional<? extends HigherTypeParameter> lookupTypeParameter(String query) {
    return Optional.empty();
  }

  public Optional<World> lookupWorldParameter(String query) {
    return Optional.empty();
  }

  public ClassOrInterfaceStaticScope getScope(HigherClassOrInterface type) {
    ClassOrInterfaceStaticScope scope = templateScopes.get(type);
    if (scope == null) {
      scope = new ClassOrInterfaceStaticScope(this, type);
      templateScopes.put(type, scope);
    }
    return scope;
  }

  private boolean hasPublicAccess(HigherClassOrInterface type) {
    return type.declarationContext() == declarationPackage || type.isPublic();
  }

  private void assertPublicAccess(HigherClassOrInterface type) {
    if (!hasPublicAccess(type)) {
      throw new StaticVerificationException(
          type.variety().labelSingular + " '" + type.identifier(true) + "' has not public access");
    }
  }
}
