package choral.diagrams;

import choral.ast.body.ClassMethodDefinition;
import choral.ast.body.InterfaceMethodDefinition;
import choral.ast.body.MethodDefinition;
import choral.ast.body.TemplateDeclaration;
import choral.ast.body.VariableDeclaration;
import choral.ast.expression.AssignExpression;
import choral.ast.expression.BinaryExpression;
import choral.ast.expression.ClassInstantiationExpression;
import choral.ast.expression.EnclosedExpression;
import choral.ast.expression.Expression;
import choral.ast.expression.FieldAccessExpression;
import choral.ast.expression.MethodCallExpression;
import choral.ast.expression.NotExpression;
import choral.ast.expression.ScopedExpression;
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
import choral.types.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Renders a typed Choral declaration directly as a Mermaid sequence diagram. */
public final class MermaidVisitor extends AbstractChoralVisitor<Void> {
    private final List<String> lines = new ArrayList<>();
    private Set<String> channels = Set.of();

    public String render(TemplateDeclaration declaration) {
        lines.clear();
        channels = Set.of();
        lines.add("sequenceDiagram");
        declaration.worldParameters().forEach(world -> lines.add(
                "participant " + participantId(world.name().identifier()) + " as "
                        + escapeMermaid(world.name().identifier())));
        if (declaration instanceof choral.ast.body.Class type)
            type.methods().forEach(this::visit);
        if (declaration instanceof choral.ast.body.Interface type)
            type.methods().forEach(this::visit);
        return String.join("\n", lines);
    }

    @Override
    public Void visit(ClassMethodDefinition method) {
        visitMethod(method, method.body().orElse(null));
        return null;
    }

    @Override
    public Void visit(InterfaceMethodDefinition method) {
        visitMethod(method, method.body().orElse(null));
        return null;
    }

    private void visitMethod(MethodDefinition method, Statement body) {
        Set<String> previousChannels = channels;
        channels = method.typeAnnotation().stream()
                .flatMap(annotation -> annotation.channels().stream())
                .map(channel -> channel.left())
                .collect(Collectors.toSet());
        visitStatement(body);
        channels = previousChannels;
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
        visitStatement(statement.ifBranch());
        visitStatement(statement.elseBranch());
        visitStatement(statement.continuation());
        return null;
    }

    @Override
    public Void visit(SwitchStatement statement) {
        visitExpression(statement.guard());
        statement.cases().values().forEach(this::visitStatement);
        visitStatement(statement.continuation());
        return null;
    }

    @Override
    public Void visit(TryCatchStatement statement) {
        visitStatement(statement.body());
        statement.catches().forEach(block -> visitStatement(block.right()));
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
        } else {
            visitExpression(expression.scopedExpression());
        }
        return null;
    }

    @Override
    public Void visit(MethodCallExpression expression) {
        expression.arguments().forEach(this::visitExpression);
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

    private void visitStatement(Statement statement) {
        if (statement != null)
            visit(statement);
    }

    private void visitExpression(Expression expression) {
        if (expression instanceof ScopedExpression || expression instanceof MethodCallExpression ||
                expression instanceof AssignExpression || expression instanceof BinaryExpression ||
                expression instanceof ClassInstantiationExpression || expression instanceof EnclosedExpression ||
                expression instanceof NotExpression)
            visit(expression);
    }

    private void addChannelEvent(Expression receiver, MethodCallExpression call) {
        String channel = receiverName(receiver);
        Member.GroundMethod method = call.methodAnnotation().orElse(null);
        if (channel == null || !channels.contains(channel) || method == null)
            return;
        boolean selection = call.isSelect();
        if (!selection && !"com".equals(call.name().identifier()))
            return;
        if (method.signature().parameters().isEmpty() ||
                method.signature().parameters().get(0).type().worldArguments().isEmpty() ||
                !(method.returnType() instanceof GroundDataType returnType) ||
                returnType.worldArguments().isEmpty())
            return;
        String from = method.signature().parameters().get(0).type().worldArguments().get(0).identifier();
        String to = returnType.worldArguments().get(0).identifier();
        lines.add(participantId(from) + (selection ? "-->>" : "->>") + participantId(to)
                + ": " + escapeMermaid(call.name().identifier()));
    }

    private static String receiverName(Expression receiver) {
        if (receiver instanceof FieldAccessExpression field)
            return field.name().identifier();
        if (receiver instanceof ScopedExpression scoped &&
                scoped.scopedExpression() instanceof FieldAccessExpression field)
            return field.name().identifier();
        return null;
    }

    private static String participantId(String value) {
        return "p_" + value.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static String escapeMermaid(String value) {
        String escaped = value.replaceAll("[\\n\\r]+", " ")
                .replaceAll("[:{};]", " ")
                .strip();
        return escaped.isEmpty() ? " " : escaped;
    }
}
