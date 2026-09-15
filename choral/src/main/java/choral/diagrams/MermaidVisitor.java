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
import choral.ast.statement.SwitchStatement;
import choral.ast.statement.TryCatchStatement;
import choral.ast.statement.VariableDeclarationStatement;
import choral.ast.visitors.AbstractChoralVisitor;
import choral.types.GroundDataType;
import choral.types.GroundInterface;
import choral.types.Member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Renders the given Choral method as Mermaid sequence diagram source code.
 * The visitor appends complete Mermaid statements to {@link #diagramLines} while traversing
 * statements, expressions, and source-backed method calls in execution order.
 */
public final class MermaidVisitor extends AbstractChoralVisitor<Void> {
    /** Complete Mermaid source lines accumulated during the current render. */
    private final List<String> diagramLines = new ArrayList<>();
    private final Set<MethodDefinition> localMethods;
    /** Methods on the current expansion stack, used to stop recursive expansion. */
    private final Set<MethodDefinition> activeMethods;
    /** Converts typed AST fragments to grounded, Mermaid-safe labels. */
    private final MermaidLabels labels;
    /** Root participant worlds, in declaration order. */
    private final List<String> participantWorlds;
    /** Name of the root declaration being rendered. */
    private final String rootDeclarationName;
    private final int helperExpansionDepth;
    private int currentHelperDepth;

    public static String render(
            TemplateDeclaration declaration, MethodDefinition method,
            int helperExpansionDepth) {
        Objects.requireNonNull(declaration, "declaration");
        Objects.requireNonNull(method, "method");
        if (helperExpansionDepth < 0)
            throw new IllegalArgumentException("Helper expansion depth must not be negative");
        return new MermaidVisitor(declaration, helperExpansionDepth).render(method);
    }

    private MermaidVisitor(TemplateDeclaration declaration, int helperExpansionDepth) {
        localMethods = Collections.newSetFromMap(new IdentityHashMap<>());
        if (declaration instanceof choral.ast.body.Class type)
            localMethods.addAll(type.methods());
        if (declaration instanceof choral.ast.body.Interface type)
            localMethods.addAll(type.methods());
        activeMethods = Collections.newSetFromMap(new IdentityHashMap<>());
        labels = new MermaidLabels(declaration);
        participantWorlds = declaration.worldParameters().stream()
                .map(world -> world.name().identifier())
                .toList();
        diagramLines.add("sequenceDiagram");
        for (int index = 0; index < participantWorlds.size(); index++)
            diagramLines.add("participant p" + index + " as "
                    + labels.escape(participantWorlds.get(index)));
        rootDeclarationName = declaration.name().identifier();
        this.helperExpansionDepth = helperExpansionDepth;
    }

    private String render(MethodDefinition method) {
        if (!localMethods.contains(method))
            throw new IllegalArgumentException("Method '"
                    + method.signature().name().identifier()
                    + "' does not belong to declaration '" + rootDeclarationName + "'");
        addNote(rootDeclarationName + "." + labels.method(method));
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
                labels.expression(statement.condition()),
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
                    labels.switchCase(statement.guard(), switchCase.getKey()),
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
            branches.add(new Branch(labels.catchLabel(catchBlock.left()),
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
            appendChannelEvent(expression.scope(), call);
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
        Member.GroundMethod method = call.methodAnnotation().orElseThrow(() ->
                new IllegalStateException("Method call has no resolved method annotation: "
                        + call.name().identifier()));
        var source = method.higherCallable().sourceCode().orElse(null);
        if (!(source instanceof MethodDefinition definition) || !hasSourceBody(definition))
            return;
        labels.withMethodWorlds(definition, method, () -> visitSourceMethod(definition));
    }

    private void visitSourceMethod(MethodDefinition method) {
        String methodName = localMethods.contains(method)
                ? labels.method(method) : labels.qualifiedMethod(method);
        if (activeMethods.contains(method)) {
            addNote("recursive call to " + methodName + " omitted");
            return;
        }
        if (currentHelperDepth >= helperExpansionDepth) {
            addNote("call " + methodName);
            return;
        }
        activeMethods.add(method);
        currentHelperDepth++;
        List<String> body;
        try {
            body = capture(() -> method.accept(this));
        } finally {
            currentHelperDepth--;
            activeMethods.remove(method);
        }
        if (!body.isEmpty()) {
            diagramLines.add("rect rgba(0, 0, 0, 0.05)");
            addNote("call " + methodName);
            diagramLines.addAll(body);
            diagramLines.add("end");
        }
    }

    private static boolean hasSourceBody(MethodDefinition method) {
        if (method.hasPosition() && method.position().sourceFile() != null
                && method.position().sourceFile().endsWith(".chh"))
            return false;
        if (method instanceof ClassMethodDefinition definition)
            return definition.body().isPresent();
        if (method instanceof InterfaceMethodDefinition definition)
            return definition.body().isPresent();
        return false;
    }

    private void addNote(String text) {
        if (participantWorlds.isEmpty())
            return;
        String over = participantId(participantWorlds.get(0));
        if (participantWorlds.size() > 1)
            over += "," + participantId(participantWorlds.get(participantWorlds.size() - 1));
        diagramLines.add("Note over " + over + ": " + labels.escape(text));
    }

    private void appendChannelEvent(Expression receiver, MethodCallExpression call) {
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
        String from = labels.world(
                method.signature().parameters().get(0).type().worldArguments().get(0).identifier());
        String to = labels.world(returnType.worldArguments().get(0).identifier());
        diagramLines.add(participantId(from) + (selection ? "-->>" : "->>") + participantId(to)
                + ": " + labels.event(call));
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

    private String participantId(String world) {
        int index = participantWorlds.indexOf(world);
        if (index < 0)
            throw new IllegalStateException("World '" + world
                    + "' is not a root diagram participant");
        return "p" + index;
    }

}
