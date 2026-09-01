package choral.diagrams;

import choral.ast.CompilationUnit;
import choral.ast.SourceRange;
import choral.ast.body.MethodDefinition;
import choral.ast.body.TemplateDeclaration;

import java.util.Optional;
import java.util.stream.Stream;

/** Selects a choreography declaration and renders its typed AST as Mermaid. */
public final class ChoreographyDiagramProvider {
    public Optional<String> diagram(
            CompilationUnit unit, Position cursor, int helperExpansionDepth) {
        return declarationAt(unit, cursor)
                .filter(declaration -> !declaration.worldParameters().isEmpty())
                .flatMap(declaration -> methodAt(declaration, cursor)
                        .map(method -> MermaidVisitor.render(
                                declaration, method, helperExpansionDepth)));
    }

    private static Optional<TemplateDeclaration> declarationAt(
            CompilationUnit unit, Position cursor) {
        if (cursor == null)
            return Optional.empty();
        return declarations(unit).filter(declaration -> contains(declaration, cursor)).findFirst();
    }

    private static Optional<? extends MethodDefinition> methodAt(
            TemplateDeclaration declaration, Position cursor) {
        return methods(declaration).filter(method -> contains(method, cursor)).findFirst();
    }

    private static boolean contains(choral.ast.Node node, Position cursor) {
        if (cursor == null || !node.hasSourceRange())
            return false;
        SourceRange sourceRange = node.sourceRange();
        int line = cursor.line() + 1;
        return atOrAfter(line, cursor.character(), sourceRange.start().line(),
                sourceRange.start().column()) &&
                atOrBefore(line, cursor.character(), sourceRange.end().line(),
                        sourceRange.end().column());
    }

    private static boolean atOrAfter(int line, int character, int boundaryLine,
            int boundaryCharacter) {
        return line > boundaryLine || line == boundaryLine && character >= boundaryCharacter;
    }

    private static boolean atOrBefore(int line, int character, int boundaryLine,
            int boundaryCharacter) {
        return line < boundaryLine || line == boundaryLine && character <= boundaryCharacter;
    }

    private static Stream<TemplateDeclaration> declarations(CompilationUnit unit) {
        return Stream.concat(
                unit.classes().stream().map(declaration -> (TemplateDeclaration) declaration),
                unit.interfaces().stream().map(declaration -> (TemplateDeclaration) declaration));
    }

    private static Stream<? extends MethodDefinition> methods(
            TemplateDeclaration declaration) {
        if (declaration instanceof choral.ast.body.Class type)
            return type.methods().stream();
        if (declaration instanceof choral.ast.body.Interface type)
            return type.methods().stream();
        return Stream.empty();
    }

    /** Zero-based source position, independent of the LSP transport types. */
    public record Position(int line, int character) {
    }
}
