package choral.diagrams;

import choral.ast.CompilationUnit;
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
        return new MermaidVisitor().render(declaration);
    }

    private static TemplateDeclaration declarationAt(CompilationUnit unit, Position cursor) {
        if (cursor == null)
            return null;
        int line = cursor.line() + 1;
        for (TemplateDeclaration declaration : declarations(unit))
            if (declaration.hasPosition() &&
                    atOrAfter(line, cursor.character(), declaration.position().line(),
                            declaration.position().column()) &&
                    atOrBefore(line, cursor.character(), declaration.position().endLine(),
                            declaration.position().endColumn()))
                return declaration;
        return null;
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
