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
import choral.ast.expression.EnumCaseInstantiationExpression;
import choral.ast.expression.Expression;
import choral.ast.expression.FieldAccessExpression;
import choral.ast.expression.MethodCallExpression;
import choral.ast.expression.NotExpression;
import choral.ast.expression.ScopedExpression;
import choral.ast.expression.StaticAccessExpression;
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
import choral.types.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Renders a typed Choral declaration directly as a Mermaid sequence diagram. */
public final class MermaidVisitor extends AbstractChoralVisitor<Void> {
    private final List<String> lines = new ArrayList<>();
    private final PrettyPrinterVisitor prettyPrinter = new PrettyPrinterVisitor();
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
        lines.add("alt " + escapeMermaid(prettyPrinter.visit(statement.condition())));
        visitStatement(statement.ifBranch());
        if (!(statement.elseBranch() instanceof NilStatement)) {
            lines.add("else");
            visitStatement(statement.elseBranch());
        }
        lines.add("end");
        visitStatement(statement.continuation());
        return null;
    }

    @Override
    public Void visit(SwitchStatement statement) {
        visitExpression(statement.guard());
        boolean first = true;
        for (var switchCase : statement.cases().entrySet()) {
            lines.add((first ? "alt " : "else ")
                    + switchCaseLabel(statement.guard(), switchCase.getKey()));
            visitStatement(switchCase.getValue());
            first = false;
        }
        if (!first)
            lines.add("end");
        visitStatement(statement.continuation());
        return null;
    }

    @Override
    public Void visit(TryCatchStatement statement) {
        lines.add("critical try");
        visitStatement(statement.body());
        for (var catchBlock : statement.catches()) {
            lines.add("option catch "
                    + escapeMermaid(prettyPrinter.visit(catchBlock.left(), " ")));
            visitStatement(catchBlock.right());
        }
        lines.add("end");
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
                + ": " + eventLabel(call));
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
            return enumCase.name().identifier() + "@" + enumCase.world().name().identifier()
                    + "." + enumCase._case().identifier();
        if (expression instanceof ScopedExpression scoped &&
                scoped.scope() instanceof StaticAccessExpression staticAccess &&
                staticAccess.typeExpression().worldArguments().size() == 1 &&
                scoped.scopedExpression() instanceof FieldAccessExpression field)
            return staticAccess.typeExpression().name().identifier() + "@"
                    + staticAccess.typeExpression().worldArguments().get(0).name().identifier()
                    + "." + field.name().identifier();
        return prettyPrinter.visit(expression);
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
        return escapeMermaid(prettyPrinter.visit(guard)) + " = " + escapeMermaid(value);
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
        String escaped = value.replaceAll("[\\p{Cc}\\p{Zl}\\p{Zp}]+", " ")
                .replaceAll("[:{};<>`]", " ")
                .replaceAll("%{2,}", "%")
                .replaceAll("\\s+", " ")
                .strip();
        return escaped.isEmpty() ? " " : escaped;
    }
}
