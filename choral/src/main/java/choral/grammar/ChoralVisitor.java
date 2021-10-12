// Generated from Choral.g4 by ANTLR 4.5.3
package choral.grammar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ChoralParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ChoralVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ChoralParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(ChoralParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#referenceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferenceType(ChoralParser.ReferenceTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#typeParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameter(ChoralParser.TypeParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#worldParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorldParameter(ChoralParser.WorldParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#worldArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorldArgument(ChoralParser.WorldArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#worldArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorldArguments(ChoralParser.WorldArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#worldArgumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorldArgumentList(ChoralParser.WorldArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#typeBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeBound(ChoralParser.TypeBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#additionalBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditionalBound(ChoralParser.AdditionalBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#typeArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArguments(ChoralParser.TypeArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#typeArgumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArgumentList(ChoralParser.TypeArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#expressionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionName(ChoralParser.ExpressionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#ambiguousName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAmbiguousName(ChoralParser.AmbiguousNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(ChoralParser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#headerDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHeaderDeclaration(ChoralParser.HeaderDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#packageDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageDeclaration(ChoralParser.PackageDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#importDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportDeclaration(ChoralParser.ImportDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedName(ChoralParser.QualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#typeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDeclaration(ChoralParser.TypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#annotations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotations(ChoralParser.AnnotationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#annotationValues}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationValues(ChoralParser.AnnotationValuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#classDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassDeclaration(ChoralParser.ClassDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#classModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassModifier(ChoralParser.ClassModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#worldParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorldParameters(ChoralParser.WorldParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#typeParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameters(ChoralParser.TypeParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#typeParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameterList(ChoralParser.TypeParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#worldParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorldParameterList(ChoralParser.WorldParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#superInterfaces}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSuperInterfaces(ChoralParser.SuperInterfacesContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#interfaceTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceTypeList(ChoralParser.InterfaceTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#superClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSuperClass(ChoralParser.SuperClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#classBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassBody(ChoralParser.ClassBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassBodyDeclaration(ChoralParser.ClassBodyDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#classMemberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassMemberDeclaration(ChoralParser.ClassMemberDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldDeclaration(ChoralParser.FieldDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#fieldModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldModifier(ChoralParser.FieldModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#methodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodDeclaration(ChoralParser.MethodDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#methodModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodModifier(ChoralParser.MethodModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#methodHeader}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodHeader(ChoralParser.MethodHeaderContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#result}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResult(ChoralParser.ResultContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#formalParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameterList(ChoralParser.FormalParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#formalParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameters(ChoralParser.FormalParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#formalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameter(ChoralParser.FormalParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#methodBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodBody(ChoralParser.MethodBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorDeclaration(ChoralParser.ConstructorDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#constructorModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorModifier(ChoralParser.ConstructorModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#constructorDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorDeclarator(ChoralParser.ConstructorDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#constructorBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorBody(ChoralParser.ConstructorBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#explicitConstructorInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplicitConstructorInvocation(ChoralParser.ExplicitConstructorInvocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceDeclaration(ChoralParser.InterfaceDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#interfaceModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceModifier(ChoralParser.InterfaceModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#enumDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumDeclaration(ChoralParser.EnumDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#extendsInterfaces}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtendsInterfaces(ChoralParser.ExtendsInterfacesContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#interfaceBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceBody(ChoralParser.InterfaceBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#enumBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumBody(ChoralParser.EnumBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#enumConstantList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstantList(ChoralParser.EnumConstantListContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#enumConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstant(ChoralParser.EnumConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMethodDeclaration(ChoralParser.InterfaceMethodDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#interfaceMethodModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMethodModifier(ChoralParser.InterfaceMethodModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(ChoralParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#blockStatements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockStatements(ChoralParser.BlockStatementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#blockStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockStatement(ChoralParser.BlockStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#localVariableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocalVariableDeclaration(ChoralParser.LocalVariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#localVariableDeclarationAndAssignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocalVariableDeclarationAndAssignment(ChoralParser.LocalVariableDeclarationAndAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(ChoralParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#basicStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBasicStatement(ChoralParser.BasicStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#emptyStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyStatement(ChoralParser.EmptyStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#expressionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionStatement(ChoralParser.ExpressionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#statementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementExpression(ChoralParser.StatementExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#trailingExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrailingExpression(ChoralParser.TrailingExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#trailExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrailExpression(ChoralParser.TrailExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#thisOrSuperMethodAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThisOrSuperMethodAccess(ChoralParser.ThisOrSuperMethodAccessContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#chainedExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChainedExpression(ChoralParser.ChainedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#chainedInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChainedInvocation(ChoralParser.ChainedInvocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#chainedMethodInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChainedMethodInvocation(ChoralParser.ChainedMethodInvocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#chainedStaticMethodInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChainedStaticMethodInvocation(ChoralParser.ChainedStaticMethodInvocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#chainedClassInstanceCreation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChainedClassInstanceCreation(ChoralParser.ChainedClassInstanceCreationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#methodInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodInvocation(ChoralParser.MethodInvocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#staticGenericAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStaticGenericAccess(ChoralParser.StaticGenericAccessContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(ChoralParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#classInstanceCreationExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassInstanceCreationExpression(ChoralParser.ClassInstanceCreationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#enumCaseCreationExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumCaseCreationExpression(ChoralParser.EnumCaseCreationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#fieldAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAccess(ChoralParser.FieldAccessContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#fieldAccess_no_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAccess_no_primary(ChoralParser.FieldAccess_no_primaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(ChoralParser.ArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#ifThenStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfThenStatement(ChoralParser.IfThenStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#ifThenElseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfThenElseStatement(ChoralParser.IfThenElseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#switchStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchStatement(ChoralParser.SwitchStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#switchBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchBlock(ChoralParser.SwitchBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#switchCase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchCase(ChoralParser.SwitchCaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#switchArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchArgs(ChoralParser.SwitchArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#tryCatchStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTryCatchStatement(ChoralParser.TryCatchStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#catchBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchBlock(ChoralParser.CatchBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#returnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(ChoralParser.ReturnStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(ChoralParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(ChoralParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#leftHandSide}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeftHandSide(ChoralParser.LeftHandSideContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#assignmentOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentOperator(ChoralParser.AssignmentOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#shortCircuitOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShortCircuitOrExpression(ChoralParser.ShortCircuitOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#shortCircuitAndExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShortCircuitAndExpression(ChoralParser.ShortCircuitAndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#orExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpression(ChoralParser.OrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#andExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpression(ChoralParser.AndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#equalityExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualityExpression(ChoralParser.EqualityExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#relationalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalExpression(ChoralParser.RelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#additiveExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpression(ChoralParser.AdditiveExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpression(ChoralParser.MultiplicativeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpression(ChoralParser.UnaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChoralParser#fwd_chain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFwd_chain(ChoralParser.Fwd_chainContext ctx);
}