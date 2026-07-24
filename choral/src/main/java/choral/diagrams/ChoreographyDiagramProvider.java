package choral.diagrams;

import choral.ast.CompilationUnit;
import choral.ast.body.TemplateDeclaration;
import choral.ast.type.FormalWorldParameter;
import choral.compiler.Parser;
import choral.diagrams.ChoreographyDiagram.Message;
import choral.diagrams.ChoreographyDiagram.Participant;
import choral.diagrams.ChoreographyDiagram.Position;
import choral.diagrams.ChoreographyDiagram.Range;
import choral.diagrams.ChoreographyDiagram.Symbol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Builds a format-neutral choreography diagram from Choral source. */
public final class ChoreographyDiagramProvider {
    private static final Pattern CHANNEL = Pattern.compile(
            "(?:[A-Za-z_][\\w.]*)\\s*@\\(\\s*([A-Za-z_]\\w*)\\s*,\\s*([A-Za-z_]\\w*)\\s*\\)\\s*(?:<[^;=()]*>)?\\s+([A-Za-z_]\\w*)");
    private static final Pattern LOCATED_VALUE = Pattern.compile(
            "(?:[A-Za-z_][\\w.]*)\\s*@\\s*([A-Za-z_]\\w*)\\s+([A-Za-z_]\\w*)");
    private static final Pattern CHANNEL_CALL = Pattern.compile(
            "\\b([A-Za-z_]\\w*)\\s*\\.\\s*(?:<[^>]*>\\s*)?(com|select)\\s*\\(");

    public ChoreographyDiagram diagram(String source, Position cursor) {
        final CompilationUnit unit;
        try {
            unit = Parser.parseString(source);
        } catch (Exception exception) {
            throw new ChoreographyDiagramException(ChoreographyDiagramException.Reason.PARSE_ERROR,
                    "Unable to parse the Choral document: " + exception.getMessage(), exception);
        }
        TemplateDeclaration declaration = declarationAt(unit, cursor);
        if (declaration == null || declaration.worldParameters().isEmpty())
            throw new ChoreographyDiagramException(ChoreographyDiagramException.Reason.NO_SYMBOL,
                    "No choreography symbol was found at the cursor.");
        ChoreographyDiagram result = new ChoreographyDiagram();
        result.symbol = new Symbol(declaration.name().identifier(),
                rangeFor(source, declaration.name().identifier(), 0));
        for (FormalWorldParameter world : declaration.worldParameters())
            result.participants.add(new Participant(world.name().identifier(), world.name().identifier()));
        Map<String, Endpoints> channels = channels(source);
        Map<String, String> valueWorlds = valueWorlds(source);
        Matcher call = CHANNEL_CALL.matcher(source);
        while (call.find()) {
            Endpoints endpoints = channels.get(call.group(1));
            if (endpoints == null)
                continue;
            int close = matchingParen(source, call.end() - 1);
            String argument = close < 0 ? "" : source.substring(call.end(), close);
            String from = worldIn(argument, valueWorlds);
            String to = endpoints.other(from);
            if (from == null || to == null) {
                from = endpoints.first;
                to = endpoints.second;
            }
            result.events.add(new Message("select".equals(call.group(2)) ? "selection" : "message", from, to,
                    call.group(2), rangeAt(source, call.start(), close < 0 ? call.end() : close + 1)));
        }
        return result;
    }

    private static TemplateDeclaration declarationAt(CompilationUnit unit, Position cursor) {
        int line = cursor == null ? Integer.MAX_VALUE : cursor.line() + 1;
        TemplateDeclaration candidate = null;
        List<TemplateDeclaration> declarations = new ArrayList<>();
        declarations.addAll(unit.classes());
        declarations.addAll(unit.interfaces());
        for (TemplateDeclaration declaration : declarations)
            if (!declaration.hasPosition() || declaration.position().line() <= line)
                candidate = declaration;
        return candidate;
    }

    private static Map<String, Endpoints> channels(String source) {
        Map<String, Endpoints> result = new LinkedHashMap<>();
        Matcher matcher = CHANNEL.matcher(source);
        while (matcher.find())
            result.put(matcher.group(3), new Endpoints(matcher.group(1), matcher.group(2)));
        return result;
    }

    private static Map<String, String> valueWorlds(String source) {
        Map<String, String> result = new LinkedHashMap<>();
        Matcher matcher = LOCATED_VALUE.matcher(source);
        while (matcher.find())
            result.put(matcher.group(2), matcher.group(1));
        return result;
    }

    private static String worldIn(String argument, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet())
            if (Pattern.compile("\\b" + Pattern.quote(entry.getKey()) + "\\b").matcher(argument).find())
                return entry.getValue();
        Matcher annotation = Pattern.compile("@\\s*([A-Za-z_]\\w*)").matcher(argument);
        return annotation.find() ? annotation.group(1) : null;
    }

    private static int matchingParen(String source, int open) {
        int depth = 0;
        for (int i = open; i < source.length(); i++) {
            char character = source.charAt(i);
            if (character == '(')
                depth++;
            if (character == ')' && --depth == 0)
                return i;
        }
        return -1;
    }

    private static Range rangeFor(String source, String text, int from) {
        int offset = source.indexOf(text, from);
        return rangeAt(source, Math.max(offset, 0), Math.max(offset, 0) + text.length());
    }

    private static Range rangeAt(String source, int start, int end) {
        return new Range(positionAt(source, start), positionAt(source, end));
    }

    private static Position positionAt(String source, int offset) {
        int line = 0, character = 0;
        for (int i = 0; i < Math.min(offset, source.length()); i++) {
            if (source.charAt(i) == '\n') {
                line++;
                character = 0;
            } else
                character++;
        }
        return new Position(line, character);
    }

    private record Endpoints(String first, String second) {
        String other(String world) {
            return first.equals(world) ? second : second.equals(world) ? first : null;
        }
    }
}
