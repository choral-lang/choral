package choral.diagrams;

import choral.ast.CompilationUnit;
import choral.ast.SourceRange;
import choral.ast.body.MethodDefinition;
import choral.ast.body.TemplateDeclaration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Selects a choreography declaration and renders its typed AST as Mermaid. */
public final class ChoreographyDiagramProvider {
    public Optional<String> diagram(CompilationUnit unit, Position cursor) {
        return declarationAt(unit, cursor)
                .filter(declaration -> !declaration.worldParameters().isEmpty())
                .flatMap(declaration -> methodAt(declaration, cursor)
                        .map(method -> MermaidVisitor.render(declaration, method)));
    }

    private static Optional<TemplateDeclaration> declarationAt(
            CompilationUnit unit, Position cursor
    ) {
        if (cursor == null)
            return Optional.empty();
        for (TemplateDeclaration declaration : declarations(unit))
            if (contains(declaration, cursor))
                return Optional.of(declaration);
        return Optional.empty();
    }

    private static Optional<? extends MethodDefinition> methodAt(
            TemplateDeclaration declaration, Position cursor
    ) {
        List<? extends MethodDefinition> methods;
        if (declaration instanceof choral.ast.body.Class type) {
            methods = type.methods();
        } else if (declaration instanceof choral.ast.body.Interface type) {
            methods = type.methods();
        } else {
            methods = List.of();
        }
        return methods.stream().filter(method -> contains(method, cursor)).findFirst();
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

    private static List<TemplateDeclaration> declarations(CompilationUnit unit) {
        List<TemplateDeclaration> declarations = new ArrayList<>();
        declarations.addAll(unit.classes());
        declarations.addAll(unit.interfaces());
        declarations.sort((left, right) -> {
            if (!left.hasPosition())
                return right.hasPosition() ? -1 : 0;
            if (!right.hasPosition())
                return 1;
            int lines = Integer.compare(left.position().line(), right.position().line());
            return lines != 0 ? lines
                    : Integer.compare(left.position().column(), right.position().column());
        });
        return declarations;
    }

    /** Zero-based source position, independent of the LSP transport types. */
    public record Position(int line, int character) {
    }
}
