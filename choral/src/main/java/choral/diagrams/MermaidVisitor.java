package choral.diagrams;

import choral.ast.body.ClassMethodDefinition;
import choral.ast.body.InterfaceMethodDefinition;
import choral.ast.body.MethodDefinition;
import choral.ast.body.TemplateDeclaration;
import choral.ast.body.VariableDeclaration;
import choral.ast.expression.AssignExpression;
import choral.ast.expression.BinaryExpression;
import choral.ast.expression.BlankExpression;
import choral.ast.expression.ClassInstantiationExpression;
import choral.ast.expression.EnclosedExpression;
import choral.ast.expression.EnumCaseInstantiationExpression;
import choral.ast.expression.Expression;
import choral.ast.expression.FieldAccessExpression;
import choral.ast.expression.LiteralExpression;
import choral.ast.expression.MethodCallExpression;
import choral.ast.expression.NotExpression;
import choral.ast.expression.NullExpression;
import choral.ast.expression.ScopedExpression;
import choral.ast.expression.StaticAccessExpression;
import choral.ast.expression.SuperExpression;
import choral.ast.expression.ThisExpression;
import choral.ast.statement.BlockStatement;
import choral.ast.statement.ExpressionStatement;
import choral.ast.statement.IfStatement;
import choral.ast.statement.NilStatement;
import choral.ast.statement.ReturnStatement;
import choral.ast.statement.Statement;
import choral.ast.statement.SwitchArgument;
import choral.ast.statement.SwitchStatement;
import choral.ast.statement.TryCatchStatement;
import choral.ast.statement.VariableDeclarationStatement;
import choral.ast.visitors.AbstractChoralVisitor;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.types.GroundDataType;
import choral.types.GroundInterface;
import choral.types.Member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Renders a typed Choral method directly as Mermaid sequence-diagram source.
 * The visitor appends complete Mermaid statements to {@link #diagramLines} while traversing
 * statements, expressions, and source-backed method calls in execution order.
 */
public final class MermaidVisitor extends AbstractChoralVisitor<Void> {
    /** Default nesting limit for expanded helper bodies. */
    public static final int DEFAULT_MAXIMUM_HELPER_DEPTH = 16;
    /** Default total number of helper bodies expanded in one diagram. */
    public static final int DEFAULT_MAXIMUM_HELPER_EXPANSIONS = 128;

    /** Complete Mermaid source lines accumulated during the current render. */
    private final List<String> diagramLines = new ArrayList<>();
    /** Formats Choral AST fragments used as human-readable Mermaid labels. */
    private final PrettyPrinterVisitor prettyPrinter = new PrettyPrinterVisitor();
    /** Maximum number of nested source-backed method bodies to expand. */
    private final int maximumHelperDepth;
    /** Maximum total number of source-backed method bodies to expand. */
    private final int maximumHelperExpansions;
    /** Methods declared directly by the root choreography, tracked by AST identity. */
    private final Set<MethodDefinition> localMethods;
    /** Methods on the current expansion stack, used to stop recursive expansion. */
    private final Set<MethodDefinition> activeMethods;
    /** Maps worlds in the current method body to participant worlds in the root diagram. */
    private Map<String, String> worldMapping;
    /** Mermaid identifiers for root participants, in declaration order. */
    private final List<String> participantIds;
    /** Name of the root declaration being rendered. */
    private final String rootDeclarationName;
    /** Current nesting depth of expanded source-backed method bodies. */
    private int helperDepth;
    /** Total source-backed method bodies expanded during the current render. */
    private int helperExpansions;
    /** Whether the depth-limit truncation note has already been emitted. */
    private boolean helperDepthLimitReported;
    /** Whether the total-expansion-limit note has already been emitted. */
    private boolean helperCountLimitReported;

    public static String render(
            TemplateDeclaration declaration, MethodDefinition method) {
        return render(declaration, method, DEFAULT_MAXIMUM_HELPER_DEPTH,
                DEFAULT_MAXIMUM_HELPER_EXPANSIONS);
    }

    public static String render(
            TemplateDeclaration declaration, MethodDefinition method,
            int maximumHelperDepth, int maximumHelperExpansions) {
        Objects.requireNonNull(declaration, "declaration");
        Objects.requireNonNull(method, "method");
        return new MermaidVisitor(declaration, maximumHelperDepth, maximumHelperExpansions)
                .render(method);
    }

    private MermaidVisitor(
            TemplateDeclaration declaration,
            int maximumHelperDepth,
            int maximumHelperExpansions) {
        if (maximumHelperDepth < 0)
            throw new IllegalArgumentException("maximumHelperDepth must not be negative");
        if (maximumHelperExpansions < 0)
            throw new IllegalArgumentException("maximumHelperExpansions must not be negative");
        this.maximumHelperDepth = maximumHelperDepth;
        this.maximumHelperExpansions = maximumHelperExpansions;
        localMethods = Collections.newSetFromMap(new IdentityHashMap<>());
        if (declaration instanceof choral.ast.body.Class type)
            localMethods.addAll(type.methods());
        if (declaration instanceof choral.ast.body.Interface type)
            localMethods.addAll(type.methods());
        activeMethods = Collections.newSetFromMap(new IdentityHashMap<>());
        worldMapping = declaration.worldParameters().stream().collect(Collectors.toMap(
                world -> world.name().identifier(), world -> world.name().identifier()));
        participantIds = declaration.worldParameters().stream()
                .map(world -> participantId(world.name().identifier()))
                .toList();
        diagramLines.add("sequenceDiagram");
        declaration.worldParameters().forEach(world -> diagramLines.add(
                "participant " + participantId(world.name().identifier()) + " as "
                        + escapeMermaid(world.name().identifier())));
        rootDeclarationName = declaration.name().identifier();
    }

    private String render(MethodDefinition method) {
        addNote(rootDeclarationName + "." + methodLabel(method));
        activeMethods.add(method);
        try {
            method.accept(this);
        } finally {
            activeMethods.remove(method);
        }
        return String.join("\n", diagramLines);
    }

    @Override
    public Void visit(ClassMethodDefinition method) {
        visitMethod(method.body().orElse(null));
        return null;
    }

    @Override
    public Void visit(InterfaceMethodDefinition method) {
        visitMethod(method.body().orElse(null));
        return null;
    }

    private void visitMethod(Statement body) {
        visitStatement(body);
    }

    @Override
    public Void visit(Statement statement) {
        return statement.accept(this);
    }

    @Override
    public Void visit(ExpressionStatement statement) {
        visitExpression(statement.expression());
        visitStatement(statement.continuation());
        return null;
    }

    @Override
    public Void visit(VariableDeclarationStatement statement) {
        for (VariableDeclaration variable : statement.variables())
            variable.initializer().ifPresent(initializer -> visitExpression(initializer.value()));
        visitStatement(statement.continuation());
        return null;
    }

    @Override
    public Void visit(BlockStatement statement) {
        visitStatement(statement.enclosedStatement());
        visitStatement(statement.continuation());
        return null;
    }

    @Override
    public Void visit(IfStatement statement) {
        visitExpression(statement.condition());
        List<Branch> branches = new ArrayList<>();
        branches.add(new Branch(
                escapeMermaid(expressionLabel(statement.condition())),
                capture(() -> visitStatement(statement.ifBranch()))));
        if (!(statement.elseBranch() instanceof NilStatement)) {
            branches.add(new Branch("", capture(() -> visitStatement(statement.elseBranch()))));
        }
        addBlock("alt", "else", branches);
        visitStatement(statement.continuation());
        return null;
    }

    @Override
    public Void visit(SwitchStatement statement) {
        visitExpression(statement.guard());
        List<Branch> branches = new ArrayList<>();
        for (var switchCase : statement.cases().entrySet()) {
            branches.add(new Branch(
                    switchCaseLabel(statement.guard(), switchCase.getKey()),
                    capture(() -> visitStatement(switchCase.getValue()))));
        }
        addBlock("alt", "else", branches);
        visitStatement(statement.continuation());
        return null;
    }

    @Override
    public Void visit(TryCatchStatement statement) {
        List<Branch> branches = new ArrayList<>();
        branches.add(new Branch("try", capture(() -> visitStatement(statement.body()))));
        for (var catchBlock : statement.catches()) {
            branches.add(new Branch("catch "
                    + escapeMermaid(prettyPrinter.visit(catchBlock.left(), " ")),
                    capture(() -> visitStatement(catchBlock.right()))));
        }
        addBlock("critical", "option", branches);
        visitStatement(statement.continuation());
        return null;
    }

    @Override
    public Void visit(ReturnStatement statement) {
        visitExpression(statement.returnExpression());
        visitStatement(statement.continuation());
        return null;
    }

    @Override
    public Void visit(NilStatement statement) {
        return null;
    }

    @Override
    public Void visit(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public Void visit(ScopedExpression expression) {
        visitExpression(expression.scope());
        if (expression.scopedExpression() instanceof MethodCallExpression call) {
            call.arguments().forEach(this::visitExpression);
            addChannelEvent(expression.scope(), call);
            visitSourceMethod(call);
        } else {
            visitExpression(expression.scopedExpression());
        }
        return null;
    }

    @Override
    public Void visit(MethodCallExpression expression) {
        expression.arguments().forEach(this::visitExpression);
        visitSourceMethod(expression);
        return null;
    }

    @Override
    public Void visit(AssignExpression expression) {
        visitExpression(expression.target());
        visitExpression(expression.value());
        return null;
    }

    @Override
    public Void visit(BinaryExpression expression) {
        visitExpression(expression.left());
        visitExpression(expression.right());
        return null;
    }

    @Override
    public Void visit(ClassInstantiationExpression expression) {
        expression.arguments().forEach(this::visitExpression);
        return null;
    }

    @Override
    public Void visit(EnclosedExpression expression) {
        visitExpression(expression.nestedExpression());
        return null;
    }

    @Override
    public Void visit(NotExpression expression) {
        visitExpression(expression.expression());
        return null;
    }

    @Override
    public Void visit(EnumCaseInstantiationExpression expression) {
        return null;
    }

    @Override
    public Void visit(FieldAccessExpression expression) {
        return null;
    }

    @Override
    public Void visit(StaticAccessExpression expression) {
        return null;
    }

    @Override
    public Void visit(ThisExpression expression) {
        return null;
    }

    @Override
    public Void visit(SuperExpression expression) {
        return null;
    }

    @Override
    public Void visit(NullExpression expression) {
        return null;
    }

    @Override
    public Void visit(BlankExpression expression) {
        return null;
    }

    @Override
    public Void visit(LiteralExpression.BooleanLiteralExpression expression) {
        return null;
    }

    @Override
    public Void visit(LiteralExpression.IntegerLiteralExpression expression) {
        return null;
    }

    @Override
    public Void visit(LiteralExpression.DoubleLiteralExpression expression) {
        return null;
    }

    @Override
    public Void visit(LiteralExpression.StringLiteralExpression expression) {
        return null;
    }

    private void visitStatement(Statement statement) {
        if (statement != null)
            visit(statement);
    }

    private void visitExpression(Expression expression) {
        if (expression != null)
            expression.accept(this);
    }

    private record Branch(String label, List<String> lines) {
    }

    private List<String> capture(Runnable render) {
        int firstLine = diagramLines.size();
        render.run();
        List<String> captured = List.copyOf(
                diagramLines.subList(firstLine, diagramLines.size()));
        diagramLines.subList(firstLine, diagramLines.size()).clear();
        return captured;
    }

    private void addBlock(
            String firstKeyword, String nextKeyword, List<Branch> branches) {
        if (branches.stream().noneMatch(branch -> !branch.lines().isEmpty()))
            return;
        for (int index = 0; index < branches.size(); index++) {
            Branch branch = branches.get(index);
            String keyword = index == 0 ? firstKeyword : nextKeyword;
            diagramLines.add(keyword
                    + (branch.label().isEmpty() ? "" : " " + branch.label()));
            diagramLines.addAll(branch.lines());
        }
        diagramLines.add("end");
    }

    private void visitSourceMethod(MethodCallExpression call) {
        Member.GroundMethod method = call.methodAnnotation().orElse(null);
        if (method == null)
            return;
        var source = method.higherCallable().sourceCode().orElse(null);
        if (!(source instanceof MethodDefinition definition) || !hasBody(definition))
            return;
        Map<String, String> previousWorldMapping = worldMapping;
        worldMapping = groundedWorlds(definition, method);
        try {
            visitSourceMethod(definition);
        } finally {
            worldMapping = previousWorldMapping;
        }
    }

    private void visitSourceMethod(MethodDefinition method) {
        String methodName = expandedMethodLabel(method);
        if (activeMethods.contains(method)) {
            addNote("recursive call to " + methodName + " omitted");
            return;
        }
        if (helperDepth >= maximumHelperDepth) {
            if (!helperDepthLimitReported) {
                addNote("helper expansion depth limit " + maximumHelperDepth
                        + " reached - deeper helper calls omitted");
                helperDepthLimitReported = true;
            }
            return;
        }
        if (helperExpansions >= maximumHelperExpansions) {
            if (!helperCountLimitReported) {
                addNote("helper expansion count limit " + maximumHelperExpansions
                        + " reached - remaining helper calls omitted");
                helperCountLimitReported = true;
            }
            return;
        }
        activeMethods.add(method);
        helperDepth++;
        helperExpansions++;
        int contextLine = diagramLines.size();
        addNote("call " + methodName);
        boolean contextAdded = diagramLines.size() > contextLine;
        int bodyLine = diagramLines.size();
        try {
            method.accept(this);
        } finally {
            helperDepth--;
            activeMethods.remove(method);
        }
        if (diagramLines.size() == bodyLine) {
            if (contextAdded)
                diagramLines.remove(contextLine);
        } else {
            addNote("return " + methodName);
        }
    }

    private static boolean hasBody(MethodDefinition method) {
        if (method instanceof ClassMethodDefinition definition)
            return definition.body().isPresent();
        if (method instanceof InterfaceMethodDefinition definition)
            return definition.body().isPresent();
        return false;
    }

    private void addNote(String text) {
        if (participantIds.isEmpty())
            return;
        String over = participantIds.get(0);
        if (participantIds.size() > 1)
            over += "," + participantIds.get(participantIds.size() - 1);
        diagramLines.add("Note over " + over + ": " + escapeMermaid(text));
    }

    private String methodLabel(MethodDefinition method) {
        String name = method.signature().name().identifier();
        long overloads = method.typeAnnotation().stream()
                .flatMap(annotation -> annotation.declarationContext().declaredMethods())
                .filter(candidate -> candidate.identifier().equals(name))
                .count();
        if (overloads < 2)
            return name;
        return name + "(" + method.signature().parameters().stream()
                .map(parameter -> groundedTypeLabel(prettyPrinter.visit(parameter.type())))
                .collect(Collectors.joining(", ")) + ")";
    }

    private String expandedMethodLabel(MethodDefinition method) {
        String label = methodLabel(method);
        if (localMethods.contains(method))
            return label;
        return method.typeAnnotation()
                .map(annotation -> annotation.declarationContext().typeConstructor().identifier()
                        + "." + label)
                .orElse(label);
    }

    private Map<String, String> groundedWorlds(
            MethodDefinition definition, Member.GroundMethod method) {
        Map<String, String> grounded = new HashMap<>(worldMapping);
        List<? extends choral.types.World> formalWorlds = definition.typeAnnotation()
                .map(annotation -> annotation.declarationContext().worldArguments())
                .orElse(List.of());
        List<? extends choral.types.World> actualWorlds =
                method.higherCallable().declarationContext().worldArguments();
        for (int index = 0; index < Math.min(formalWorlds.size(), actualWorlds.size()); index++)
            grounded.put(formalWorlds.get(index).identifier(),
                    groundedWorld(actualWorlds.get(index).identifier()));
        return Map.copyOf(grounded);
    }

    private void addChannelEvent(Expression receiver, MethodCallExpression call) {
        if (!isChannelReceiver(receiver))
            return;
        Member.GroundMethod method = call.methodAnnotation().orElseThrow(() ->
                new IllegalStateException("Typed channel call has no resolved method: "
                        + call.name().identifier()));
        boolean selection = call.isSelect();
        if (!selection && !"com".equals(call.name().identifier()))
            return;
        if (method.signature().parameters().isEmpty() ||
                method.signature().parameters().get(0).type().worldArguments().isEmpty() ||
                !(method.returnType() instanceof GroundDataType returnType) ||
                returnType.worldArguments().isEmpty())
            return;
        String from = groundedWorld(
                method.signature().parameters().get(0).type().worldArguments().get(0).identifier());
        String to = groundedWorld(returnType.worldArguments().get(0).identifier());
        diagramLines.add(participantId(from) + (selection ? "-->>" : "->>") + participantId(to)
                + ": " + eventLabel(call));
    }

    private static boolean isChannelReceiver(Expression receiver) {
        if (!(receiver.typeAnnotation().orElse(null) instanceof GroundInterface type))
            return false;
        return isChannelInterface(type) ||
                type.allExtendedInterfaces().anyMatch(MermaidVisitor::isChannelInterface);
    }

    private static boolean isChannelInterface(GroundInterface type) {
        String name = type.typeConstructor().identifier(true);
        return "choral.channels.DiDataChannel".equals(name) ||
                "choral.channels.DiSelectChannel".equals(name);
    }

    private String groundedWorld(String world) {
        return worldMapping.getOrDefault(world, world);
    }

    private String eventLabel(MethodCallExpression call) {
        if (!call.arguments().isEmpty())
            return escapeMermaid(expressionLabel(call.arguments().get(0)));
        if (!call.typeArguments().isEmpty())
            return escapeMermaid(prettyPrinter.visit(call.typeArguments().get(0)));
        return escapeMermaid(call.name().identifier());
    }

    private String expressionLabel(Expression expression) {
        if (expression instanceof EnumCaseInstantiationExpression enumCase)
            return enumCase.name().identifier() + "@"
                    + groundedWorld(enumCase.world().name().identifier())
                    + "." + enumCase._case().identifier();
        if (expression instanceof LiteralExpression<?> literal) {
            String label = prettyPrinter.visit(literal);
            if (literal.world() != null)
                return groundedWorldReferences(label);
            return label;
        }
        if (expression instanceof ScopedExpression scoped &&
                scoped.scope() instanceof StaticAccessExpression staticAccess &&
                staticAccess.typeExpression().worldArguments().size() == 1 &&
                scoped.scopedExpression() instanceof FieldAccessExpression field)
            return staticAccess.typeExpression().name().identifier() + "@"
                    + groundedWorld(staticAccess.typeExpression().worldArguments().get(0)
                            .name().identifier())
                    + "." + field.name().identifier();
        return groundedWorldReferences(prettyPrinter.visit(expression));
    }

    private String groundedWorldReferences(String label) {
        String grounded = label;
        for (var mapping : worldMapping.entrySet())
            grounded = grounded.replaceAll(
                    "@" + Pattern.quote(mapping.getKey()) + "(?![A-Za-z0-9_$])",
                    Matcher.quoteReplacement("@" + mapping.getValue()));
        return grounded;
    }

    private String groundedTypeLabel(String label) {
        String grounded = label;
        for (var mapping : worldMapping.entrySet())
            grounded = grounded.replaceAll(
                    "(?<![A-Za-z0-9_$])" + Pattern.quote(mapping.getKey())
                            + "(?![A-Za-z0-9_$])",
                    Matcher.quoteReplacement(mapping.getValue()));
        return grounded;
    }

    private String switchCaseLabel(Expression guard, SwitchArgument<?> switchCase) {
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
        return escapeMermaid(expressionLabel(guard)) + " = "
                + escapeMermaid(groundedWorldReferences(value));
    }

    private static String participantId(String value) {
        return "p_" + value.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static String escapeMermaid(String value) {
        String escaped = value.replaceAll("[\\p{Cc}\\p{Zl}\\p{Zp}]+", " ")
                .replaceAll("[:{};<>`]", " ")
                .replaceAll("%{2,}", "%")
                .replaceAll("\\s+", " ")
                .strip();
        return escaped.isEmpty() ? " " : escaped;
    }
}
