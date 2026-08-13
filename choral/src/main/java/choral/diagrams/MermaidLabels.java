package choral.diagrams;

import choral.ast.body.MethodDefinition;
import choral.ast.body.TemplateDeclaration;
import choral.ast.body.VariableDeclaration;
import choral.ast.expression.EnumCaseInstantiationExpression;
import choral.ast.expression.Expression;
import choral.ast.expression.FieldAccessExpression;
import choral.ast.expression.LiteralExpression;
import choral.ast.expression.MethodCallExpression;
import choral.ast.expression.ScopedExpression;
import choral.ast.expression.StaticAccessExpression;
import choral.ast.statement.SwitchArgument;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.types.Member;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Converts typed Choral AST fragments into grounded, Mermaid-safe diagram labels.
 */
final class MermaidLabels {
    private final PrettyPrinterVisitor prettyPrinter = new PrettyPrinterVisitor();
    private final Set<MethodDefinition> localMethods;
    private final Deque<WorldMapping> worldMappings = new ArrayDeque<>();

    MermaidLabels(TemplateDeclaration declaration) {
        localMethods = Collections.newSetFromMap(new IdentityHashMap<>());
        if (declaration instanceof choral.ast.body.Class type)
            localMethods.addAll(type.methods());
        if (declaration instanceof choral.ast.body.Interface type)
            localMethods.addAll(type.methods());
        worldMappings.push(new WorldMapping(declaration.worldParameters().stream().collect(
                Collectors.toMap(world -> world.name().identifier(),
                        world -> world.name().identifier()))));
    }

    boolean isLocal(MethodDefinition method) {
        return localMethods.contains(method);
    }

    void withMethodWorlds(
            MethodDefinition definition, Member.GroundMethod method, Runnable render) {
        List<? extends choral.types.World> formalWorlds = definition.typeAnnotation()
                .map(annotation -> annotation.declarationContext().worldArguments())
                .orElse(List.of());
        List<? extends choral.types.World> actualWorlds =
                method.higherCallable().declarationContext().worldArguments();
        if (formalWorlds.size() != actualWorlds.size())
            throw new IllegalStateException("World arity mismatch while expanding method '"
                    + definition.signature().name().identifier() + "': expected "
                    + formalWorlds.size() + " but resolved " + actualWorlds.size());
        Map<String, String> grounded = new HashMap<>(worldMapping().worlds());
        for (int index = 0; index < formalWorlds.size(); index++)
            grounded.put(formalWorlds.get(index).identifier(),
                    world(actualWorlds.get(index).identifier()));
        worldMappings.push(new WorldMapping(Map.copyOf(grounded)));
        try {
            render.run();
        } finally {
            worldMappings.pop();
        }
    }

    String method(MethodDefinition method) {
        String name = method.signature().name().identifier();
        long overloads = method.typeAnnotation().stream()
                .flatMap(annotation -> annotation.declarationContext().declaredMethods())
                .filter(candidate -> candidate.identifier().equals(name))
                .count();
        if (overloads < 2)
            return name;
        return name + "(" + method.signature().parameters().stream()
                .map(parameter -> worldMapping().types(prettyPrinter.visit(parameter.type())))
                .collect(Collectors.joining(", ")) + ")";
    }

    String expandedMethod(MethodDefinition method) {
        String label = method(method);
        if (localMethods.contains(method))
            return label;
        return method.typeAnnotation()
                .map(annotation -> annotation.declarationContext().typeConstructor().identifier()
                        + "." + label)
                .orElse(label);
    }

    String expression(Expression expression) {
        return escape(groundedExpression(expression));
    }

    String event(MethodCallExpression call) {
        String label;
        if (!call.arguments().isEmpty()) {
            label = groundedExpression(call.arguments().get(0));
        } else if (!call.typeArguments().isEmpty()) {
            label = prettyPrinter.visit(call.typeArguments().get(0));
        } else {
            label = call.name().identifier();
        }
        return escape(label);
    }

    String switchCase(Expression guard, SwitchArgument<?> switchCase) {
        if (switchCase instanceof SwitchArgument.SwitchArgumentDefault ||
                switchCase instanceof SwitchArgument.SwitchArgumentMergeDefault)
            return "default";
        String value;
        if (switchCase instanceof SwitchArgument.SwitchArgumentLiteral literal) {
            value = prettyPrinter.visit(literal.argument());
        } else if (switchCase instanceof SwitchArgument.SwitchArgumentLabel label) {
            value = label.argument().identifier();
        } else if (switchCase instanceof SwitchArgument.SwitchArgumentClassLabel label) {
            value = label.argument().left().identifier() + " "
                    + label.argument().right().identifier();
        } else {
            value = switchCase.argument().toString();
        }
        return escape(groundedExpression(guard) + " = " + worldMapping().references(value));
    }

    String catchLabel(VariableDeclaration parameter) {
        return "catch "
                + escape(worldMapping().references(prettyPrinter.visit(parameter, " ")));
    }

    String world(String world) {
        return worldMapping().ground(world);
    }

    String escape(String value) {
        String escaped = value.replaceAll("[\\p{Cc}\\p{Zl}\\p{Zp}]+", " ")
                .replaceAll("[:{};<>`]", " ")
                .replaceAll("%{2,}", "%")
                .replaceAll("\\s+", " ")
                .strip();
        return escaped.isEmpty() ? " " : escaped;
    }

    private String groundedExpression(Expression expression) {
        if (expression instanceof EnumCaseInstantiationExpression enumCase)
            return enumCase.name().identifier() + "@"
                    + world(enumCase.world().name().identifier())
                    + "." + enumCase._case().identifier();
        if (expression instanceof LiteralExpression<?> literal) {
            String label = prettyPrinter.visit(literal);
            if (literal.world() != null)
                return worldMapping().references(label);
            return label;
        }
        if (expression instanceof ScopedExpression scoped &&
                scoped.scope() instanceof StaticAccessExpression staticAccess &&
                staticAccess.typeExpression().worldArguments().size() == 1 &&
                scoped.scopedExpression() instanceof FieldAccessExpression field)
            return staticAccess.typeExpression().name().identifier() + "@"
                    + world(staticAccess.typeExpression().worldArguments().get(0)
                            .name().identifier())
                    + "." + field.name().identifier();
        return worldMapping().references(prettyPrinter.visit(expression));
    }

    private WorldMapping worldMapping() {
        return worldMappings.element();
    }

    private record WorldMapping(Map<String, String> worlds) {
        private static final String WORLD_IDENTIFIER =
                "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
        private static final Pattern WORLD_REFERENCE = Pattern.compile(
                "@(" + WORLD_IDENTIFIER + ")|@\\(([^)]*)\\)");
        private static final Pattern IDENTIFIER = Pattern.compile(
                "(?<!\\p{javaJavaIdentifierPart})"
                        + "(" + WORLD_IDENTIFIER + ")"
                        + "(?!\\p{javaJavaIdentifierPart})");

        private String ground(String world) {
            return worlds.getOrDefault(world, world);
        }

        private String references(String label) {
            Matcher matcher = WORLD_REFERENCE.matcher(label);
            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                String replacement = matcher.group(1) != null
                        ? "@" + ground(matcher.group(1))
                        : "@(" + replace(matcher.group(2), IDENTIFIER, "") + ")";
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(result);
            return result.toString();
        }

        private String types(String label) {
            return replace(label, IDENTIFIER, "");
        }

        private String replace(String label, Pattern pattern, String prefix) {
            Matcher matcher = pattern.matcher(label);
            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                String replacement = worlds.get(matcher.group(1));
                if (replacement != null)
                    matcher.appendReplacement(result,
                            Matcher.quoteReplacement(prefix + replacement));
            }
            matcher.appendTail(result);
            return result.toString();
        }
    }
}
