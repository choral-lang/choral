package choral.diagrams;

import choral.ast.CompilationUnit;
import choral.ast.body.MethodDefinition;
import choral.ast.body.TemplateDeclaration;

import java.util.ArrayList;
import java.util.List;

/** Selects a choreography declaration and renders its typed AST as Mermaid. */
public final class ChoreographyDiagramProvider {
    public String diagram(CompilationUnit unit, Position cursor) {
        TemplateDeclaration declaration = declarationAt(unit, cursor);
        if (declaration == null || declaration.worldParameters().isEmpty())
            throw new ChoreographyDiagramException(ChoreographyDiagramException.Reason.NO_SYMBOL,
                    "No choreography symbol was found at the cursor.");
        MethodDefinition method = methodAt(declaration, cursor);
        if (method == null)
            throw new ChoreographyDiagramException(ChoreographyDiagramException.Reason.NO_SYMBOL,
                    "No choreography method was found at the cursor.");
        return new MermaidVisitor().render(declaration, method);
    }

    private static TemplateDeclaration declarationAt(CompilationUnit unit, Position cursor) {
        if (cursor == null)
            return null;
        for (TemplateDeclaration declaration : declarations(unit))
            if (contains(declaration, cursor))
                return declaration;
        return null;
    }

    private static MethodDefinition methodAt(TemplateDeclaration declaration, Position cursor) {
        List<? extends MethodDefinition> methods;
        if (declaration instanceof choral.ast.body.Class type) {
            methods = type.methods();
        } else if (declaration instanceof choral.ast.body.Interface type) {
            methods = type.methods();
        } else {
            methods = List.of();
        }
        return methods.stream().filter(method -> contains(method, cursor)).findFirst().orElse(null);
    }

    private static boolean contains(choral.ast.Node node, Position cursor) {
        if (cursor == null || !node.hasPosition())
            return false;
        int line = cursor.line() + 1;
        return atOrAfter(line, cursor.character(), node.position().line(),
                node.position().column()) &&
                atOrBefore(line, cursor.character(), node.position().endLine(),
                        node.position().endColumn());
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
