// Generated from Choral.g4 by ANTLR 4.5.3
package choral.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ChoralParser}.
 */
public interface ChoralListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ChoralParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(ChoralParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(ChoralParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#referenceType}.
	 * @param ctx the parse tree
	 */
	void enterReferenceType(ChoralParser.ReferenceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#referenceType}.
	 * @param ctx the parse tree
	 */
	void exitReferenceType(ChoralParser.ReferenceTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#typeParameter}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameter(ChoralParser.TypeParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#typeParameter}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameter(ChoralParser.TypeParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#worldParameter}.
	 * @param ctx the parse tree
	 */
	void enterWorldParameter(ChoralParser.WorldParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#worldParameter}.
	 * @param ctx the parse tree
	 */
	void exitWorldParameter(ChoralParser.WorldParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#worldArgument}.
	 * @param ctx the parse tree
	 */
	void enterWorldArgument(ChoralParser.WorldArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#worldArgument}.
	 * @param ctx the parse tree
	 */
	void exitWorldArgument(ChoralParser.WorldArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#worldArguments}.
	 * @param ctx the parse tree
	 */
	void enterWorldArguments(ChoralParser.WorldArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#worldArguments}.
	 * @param ctx the parse tree
	 */
	void exitWorldArguments(ChoralParser.WorldArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#worldArgumentList}.
	 * @param ctx the parse tree
	 */
	void enterWorldArgumentList(ChoralParser.WorldArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#worldArgumentList}.
	 * @param ctx the parse tree
	 */
	void exitWorldArgumentList(ChoralParser.WorldArgumentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#typeBound}.
	 * @param ctx the parse tree
	 */
	void enterTypeBound(ChoralParser.TypeBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#typeBound}.
	 * @param ctx the parse tree
	 */
	void exitTypeBound(ChoralParser.TypeBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#additionalBound}.
	 * @param ctx the parse tree
	 */
	void enterAdditionalBound(ChoralParser.AdditionalBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#additionalBound}.
	 * @param ctx the parse tree
	 */
	void exitAdditionalBound(ChoralParser.AdditionalBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void enterTypeArguments(ChoralParser.TypeArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void exitTypeArguments(ChoralParser.TypeArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#typeArgumentList}.
	 * @param ctx the parse tree
	 */
	void enterTypeArgumentList(ChoralParser.TypeArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#typeArgumentList}.
	 * @param ctx the parse tree
	 */
	void exitTypeArgumentList(ChoralParser.TypeArgumentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#expressionName}.
	 * @param ctx the parse tree
	 */
	void enterExpressionName(ChoralParser.ExpressionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#expressionName}.
	 * @param ctx the parse tree
	 */
	void exitExpressionName(ChoralParser.ExpressionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#ambiguousName}.
	 * @param ctx the parse tree
	 */
	void enterAmbiguousName(ChoralParser.AmbiguousNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#ambiguousName}.
	 * @param ctx the parse tree
	 */
	void exitAmbiguousName(ChoralParser.AmbiguousNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(ChoralParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(ChoralParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#headerDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterHeaderDeclaration(ChoralParser.HeaderDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#headerDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitHeaderDeclaration(ChoralParser.HeaderDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#packageDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterPackageDeclaration(ChoralParser.PackageDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#packageDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitPackageDeclaration(ChoralParser.PackageDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterImportDeclaration(ChoralParser.ImportDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitImportDeclaration(ChoralParser.ImportDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(ChoralParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(ChoralParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#typeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterTypeDeclaration(ChoralParser.TypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#typeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitTypeDeclaration(ChoralParser.TypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#annotations}.
	 * @param ctx the parse tree
	 */
	void enterAnnotations(ChoralParser.AnnotationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#annotations}.
	 * @param ctx the parse tree
	 */
	void exitAnnotations(ChoralParser.AnnotationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#annotationValues}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationValues(ChoralParser.AnnotationValuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#annotationValues}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationValues(ChoralParser.AnnotationValuesContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterClassDeclaration(ChoralParser.ClassDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitClassDeclaration(ChoralParser.ClassDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#classModifier}.
	 * @param ctx the parse tree
	 */
	void enterClassModifier(ChoralParser.ClassModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#classModifier}.
	 * @param ctx the parse tree
	 */
	void exitClassModifier(ChoralParser.ClassModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#worldParameters}.
	 * @param ctx the parse tree
	 */
	void enterWorldParameters(ChoralParser.WorldParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#worldParameters}.
	 * @param ctx the parse tree
	 */
	void exitWorldParameters(ChoralParser.WorldParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#typeParameters}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameters(ChoralParser.TypeParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#typeParameters}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameters(ChoralParser.TypeParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#typeParameterList}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameterList(ChoralParser.TypeParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#typeParameterList}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameterList(ChoralParser.TypeParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#worldParameterList}.
	 * @param ctx the parse tree
	 */
	void enterWorldParameterList(ChoralParser.WorldParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#worldParameterList}.
	 * @param ctx the parse tree
	 */
	void exitWorldParameterList(ChoralParser.WorldParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#superInterfaces}.
	 * @param ctx the parse tree
	 */
	void enterSuperInterfaces(ChoralParser.SuperInterfacesContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#superInterfaces}.
	 * @param ctx the parse tree
	 */
	void exitSuperInterfaces(ChoralParser.SuperInterfacesContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#interfaceTypeList}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceTypeList(ChoralParser.InterfaceTypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#interfaceTypeList}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceTypeList(ChoralParser.InterfaceTypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#superClass}.
	 * @param ctx the parse tree
	 */
	void enterSuperClass(ChoralParser.SuperClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#superClass}.
	 * @param ctx the parse tree
	 */
	void exitSuperClass(ChoralParser.SuperClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#classBody}.
	 * @param ctx the parse tree
	 */
	void enterClassBody(ChoralParser.ClassBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#classBody}.
	 * @param ctx the parse tree
	 */
	void exitClassBody(ChoralParser.ClassBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterClassBodyDeclaration(ChoralParser.ClassBodyDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitClassBodyDeclaration(ChoralParser.ClassBodyDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#classMemberDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterClassMemberDeclaration(ChoralParser.ClassMemberDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#classMemberDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitClassMemberDeclaration(ChoralParser.ClassMemberDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFieldDeclaration(ChoralParser.FieldDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFieldDeclaration(ChoralParser.FieldDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#fieldModifier}.
	 * @param ctx the parse tree
	 */
	void enterFieldModifier(ChoralParser.FieldModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#fieldModifier}.
	 * @param ctx the parse tree
	 */
	void exitFieldModifier(ChoralParser.FieldModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterMethodDeclaration(ChoralParser.MethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitMethodDeclaration(ChoralParser.MethodDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#methodModifier}.
	 * @param ctx the parse tree
	 */
	void enterMethodModifier(ChoralParser.MethodModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#methodModifier}.
	 * @param ctx the parse tree
	 */
	void exitMethodModifier(ChoralParser.MethodModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#methodHeader}.
	 * @param ctx the parse tree
	 */
	void enterMethodHeader(ChoralParser.MethodHeaderContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#methodHeader}.
	 * @param ctx the parse tree
	 */
	void exitMethodHeader(ChoralParser.MethodHeaderContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#result}.
	 * @param ctx the parse tree
	 */
	void enterResult(ChoralParser.ResultContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#result}.
	 * @param ctx the parse tree
	 */
	void exitResult(ChoralParser.ResultContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameterList(ChoralParser.FormalParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameterList(ChoralParser.FormalParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameters(ChoralParser.FormalParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameters(ChoralParser.FormalParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameter(ChoralParser.FormalParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameter(ChoralParser.FormalParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#methodBody}.
	 * @param ctx the parse tree
	 */
	void enterMethodBody(ChoralParser.MethodBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#methodBody}.
	 * @param ctx the parse tree
	 */
	void exitMethodBody(ChoralParser.MethodBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterConstructorDeclaration(ChoralParser.ConstructorDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitConstructorDeclaration(ChoralParser.ConstructorDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#constructorModifier}.
	 * @param ctx the parse tree
	 */
	void enterConstructorModifier(ChoralParser.ConstructorModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#constructorModifier}.
	 * @param ctx the parse tree
	 */
	void exitConstructorModifier(ChoralParser.ConstructorModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#constructorDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterConstructorDeclarator(ChoralParser.ConstructorDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#constructorDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitConstructorDeclarator(ChoralParser.ConstructorDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#constructorBody}.
	 * @param ctx the parse tree
	 */
	void enterConstructorBody(ChoralParser.ConstructorBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#constructorBody}.
	 * @param ctx the parse tree
	 */
	void exitConstructorBody(ChoralParser.ConstructorBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#explicitConstructorInvocation}.
	 * @param ctx the parse tree
	 */
	void enterExplicitConstructorInvocation(ChoralParser.ExplicitConstructorInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#explicitConstructorInvocation}.
	 * @param ctx the parse tree
	 */
	void exitExplicitConstructorInvocation(ChoralParser.ExplicitConstructorInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceDeclaration(ChoralParser.InterfaceDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceDeclaration(ChoralParser.InterfaceDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#interfaceModifier}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceModifier(ChoralParser.InterfaceModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#interfaceModifier}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceModifier(ChoralParser.InterfaceModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#enumDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterEnumDeclaration(ChoralParser.EnumDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#enumDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitEnumDeclaration(ChoralParser.EnumDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#extendsInterfaces}.
	 * @param ctx the parse tree
	 */
	void enterExtendsInterfaces(ChoralParser.ExtendsInterfacesContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#extendsInterfaces}.
	 * @param ctx the parse tree
	 */
	void exitExtendsInterfaces(ChoralParser.ExtendsInterfacesContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#interfaceBody}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceBody(ChoralParser.InterfaceBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#interfaceBody}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceBody(ChoralParser.InterfaceBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#enumBody}.
	 * @param ctx the parse tree
	 */
	void enterEnumBody(ChoralParser.EnumBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#enumBody}.
	 * @param ctx the parse tree
	 */
	void exitEnumBody(ChoralParser.EnumBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#enumConstantList}.
	 * @param ctx the parse tree
	 */
	void enterEnumConstantList(ChoralParser.EnumConstantListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#enumConstantList}.
	 * @param ctx the parse tree
	 */
	void exitEnumConstantList(ChoralParser.EnumConstantListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#enumConstant}.
	 * @param ctx the parse tree
	 */
	void enterEnumConstant(ChoralParser.EnumConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#enumConstant}.
	 * @param ctx the parse tree
	 */
	void exitEnumConstant(ChoralParser.EnumConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceMethodDeclaration(ChoralParser.InterfaceMethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceMethodDeclaration(ChoralParser.InterfaceMethodDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#interfaceMethodModifier}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceMethodModifier(ChoralParser.InterfaceMethodModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#interfaceMethodModifier}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceMethodModifier(ChoralParser.InterfaceMethodModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(ChoralParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(ChoralParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#blockStatements}.
	 * @param ctx the parse tree
	 */
	void enterBlockStatements(ChoralParser.BlockStatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#blockStatements}.
	 * @param ctx the parse tree
	 */
	void exitBlockStatements(ChoralParser.BlockStatementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void enterBlockStatement(ChoralParser.BlockStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void exitBlockStatement(ChoralParser.BlockStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#localVariableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterLocalVariableDeclaration(ChoralParser.LocalVariableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#localVariableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitLocalVariableDeclaration(ChoralParser.LocalVariableDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#localVariableDeclarationAndAssignment}.
	 * @param ctx the parse tree
	 */
	void enterLocalVariableDeclarationAndAssignment(ChoralParser.LocalVariableDeclarationAndAssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#localVariableDeclarationAndAssignment}.
	 * @param ctx the parse tree
	 */
	void exitLocalVariableDeclarationAndAssignment(ChoralParser.LocalVariableDeclarationAndAssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(ChoralParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(ChoralParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#basicStatement}.
	 * @param ctx the parse tree
	 */
	void enterBasicStatement(ChoralParser.BasicStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#basicStatement}.
	 * @param ctx the parse tree
	 */
	void exitBasicStatement(ChoralParser.BasicStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#emptyStatement}.
	 * @param ctx the parse tree
	 */
	void enterEmptyStatement(ChoralParser.EmptyStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#emptyStatement}.
	 * @param ctx the parse tree
	 */
	void exitEmptyStatement(ChoralParser.EmptyStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void enterExpressionStatement(ChoralParser.ExpressionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void exitExpressionStatement(ChoralParser.ExpressionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#statementExpression}.
	 * @param ctx the parse tree
	 */
	void enterStatementExpression(ChoralParser.StatementExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#statementExpression}.
	 * @param ctx the parse tree
	 */
	void exitStatementExpression(ChoralParser.StatementExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#trailingExpression}.
	 * @param ctx the parse tree
	 */
	void enterTrailingExpression(ChoralParser.TrailingExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#trailingExpression}.
	 * @param ctx the parse tree
	 */
	void exitTrailingExpression(ChoralParser.TrailingExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#trailExpression}.
	 * @param ctx the parse tree
	 */
	void enterTrailExpression(ChoralParser.TrailExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#trailExpression}.
	 * @param ctx the parse tree
	 */
	void exitTrailExpression(ChoralParser.TrailExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#thisOrSuperMethodAccess}.
	 * @param ctx the parse tree
	 */
	void enterThisOrSuperMethodAccess(ChoralParser.ThisOrSuperMethodAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#thisOrSuperMethodAccess}.
	 * @param ctx the parse tree
	 */
	void exitThisOrSuperMethodAccess(ChoralParser.ThisOrSuperMethodAccessContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#chainedExpression}.
	 * @param ctx the parse tree
	 */
	void enterChainedExpression(ChoralParser.ChainedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#chainedExpression}.
	 * @param ctx the parse tree
	 */
	void exitChainedExpression(ChoralParser.ChainedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#chainedInvocation}.
	 * @param ctx the parse tree
	 */
	void enterChainedInvocation(ChoralParser.ChainedInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#chainedInvocation}.
	 * @param ctx the parse tree
	 */
	void exitChainedInvocation(ChoralParser.ChainedInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#chainedMethodInvocation}.
	 * @param ctx the parse tree
	 */
	void enterChainedMethodInvocation(ChoralParser.ChainedMethodInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#chainedMethodInvocation}.
	 * @param ctx the parse tree
	 */
	void exitChainedMethodInvocation(ChoralParser.ChainedMethodInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#chainedStaticMethodInvocation}.
	 * @param ctx the parse tree
	 */
	void enterChainedStaticMethodInvocation(ChoralParser.ChainedStaticMethodInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#chainedStaticMethodInvocation}.
	 * @param ctx the parse tree
	 */
	void exitChainedStaticMethodInvocation(ChoralParser.ChainedStaticMethodInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#chainedClassInstanceCreation}.
	 * @param ctx the parse tree
	 */
	void enterChainedClassInstanceCreation(ChoralParser.ChainedClassInstanceCreationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#chainedClassInstanceCreation}.
	 * @param ctx the parse tree
	 */
	void exitChainedClassInstanceCreation(ChoralParser.ChainedClassInstanceCreationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#methodInvocation}.
	 * @param ctx the parse tree
	 */
	void enterMethodInvocation(ChoralParser.MethodInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#methodInvocation}.
	 * @param ctx the parse tree
	 */
	void exitMethodInvocation(ChoralParser.MethodInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#staticGenericAccess}.
	 * @param ctx the parse tree
	 */
	void enterStaticGenericAccess(ChoralParser.StaticGenericAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#staticGenericAccess}.
	 * @param ctx the parse tree
	 */
	void exitStaticGenericAccess(ChoralParser.StaticGenericAccessContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(ChoralParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(ChoralParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#classInstanceCreationExpression}.
	 * @param ctx the parse tree
	 */
	void enterClassInstanceCreationExpression(ChoralParser.ClassInstanceCreationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#classInstanceCreationExpression}.
	 * @param ctx the parse tree
	 */
	void exitClassInstanceCreationExpression(ChoralParser.ClassInstanceCreationExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#enumCaseCreationExpression}.
	 * @param ctx the parse tree
	 */
	void enterEnumCaseCreationExpression(ChoralParser.EnumCaseCreationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#enumCaseCreationExpression}.
	 * @param ctx the parse tree
	 */
	void exitEnumCaseCreationExpression(ChoralParser.EnumCaseCreationExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#fieldAccess}.
	 * @param ctx the parse tree
	 */
	void enterFieldAccess(ChoralParser.FieldAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#fieldAccess}.
	 * @param ctx the parse tree
	 */
	void exitFieldAccess(ChoralParser.FieldAccessContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#fieldAccess_no_primary}.
	 * @param ctx the parse tree
	 */
	void enterFieldAccess_no_primary(ChoralParser.FieldAccess_no_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#fieldAccess_no_primary}.
	 * @param ctx the parse tree
	 */
	void exitFieldAccess_no_primary(ChoralParser.FieldAccess_no_primaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentList(ChoralParser.ArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentList(ChoralParser.ArgumentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#ifThenStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfThenStatement(ChoralParser.IfThenStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#ifThenStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfThenStatement(ChoralParser.IfThenStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#ifThenElseStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfThenElseStatement(ChoralParser.IfThenElseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#ifThenElseStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfThenElseStatement(ChoralParser.IfThenElseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#switchStatement}.
	 * @param ctx the parse tree
	 */
	void enterSwitchStatement(ChoralParser.SwitchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#switchStatement}.
	 * @param ctx the parse tree
	 */
	void exitSwitchStatement(ChoralParser.SwitchStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#switchBlock}.
	 * @param ctx the parse tree
	 */
	void enterSwitchBlock(ChoralParser.SwitchBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#switchBlock}.
	 * @param ctx the parse tree
	 */
	void exitSwitchBlock(ChoralParser.SwitchBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#switchCase}.
	 * @param ctx the parse tree
	 */
	void enterSwitchCase(ChoralParser.SwitchCaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#switchCase}.
	 * @param ctx the parse tree
	 */
	void exitSwitchCase(ChoralParser.SwitchCaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#switchArgs}.
	 * @param ctx the parse tree
	 */
	void enterSwitchArgs(ChoralParser.SwitchArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#switchArgs}.
	 * @param ctx the parse tree
	 */
	void exitSwitchArgs(ChoralParser.SwitchArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#tryCatchStatement}.
	 * @param ctx the parse tree
	 */
	void enterTryCatchStatement(ChoralParser.TryCatchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#tryCatchStatement}.
	 * @param ctx the parse tree
	 */
	void exitTryCatchStatement(ChoralParser.TryCatchStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#catchBlock}.
	 * @param ctx the parse tree
	 */
	void enterCatchBlock(ChoralParser.CatchBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#catchBlock}.
	 * @param ctx the parse tree
	 */
	void exitCatchBlock(ChoralParser.CatchBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(ChoralParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(ChoralParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(ChoralParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(ChoralParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(ChoralParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(ChoralParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#leftHandSide}.
	 * @param ctx the parse tree
	 */
	void enterLeftHandSide(ChoralParser.LeftHandSideContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#leftHandSide}.
	 * @param ctx the parse tree
	 */
	void exitLeftHandSide(ChoralParser.LeftHandSideContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#assignmentOperator}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentOperator(ChoralParser.AssignmentOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#assignmentOperator}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentOperator(ChoralParser.AssignmentOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#shortCircuitOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterShortCircuitOrExpression(ChoralParser.ShortCircuitOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#shortCircuitOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitShortCircuitOrExpression(ChoralParser.ShortCircuitOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#shortCircuitAndExpression}.
	 * @param ctx the parse tree
	 */
	void enterShortCircuitAndExpression(ChoralParser.ShortCircuitAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#shortCircuitAndExpression}.
	 * @param ctx the parse tree
	 */
	void exitShortCircuitAndExpression(ChoralParser.ShortCircuitAndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#orExpression}.
	 * @param ctx the parse tree
	 */
	void enterOrExpression(ChoralParser.OrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#orExpression}.
	 * @param ctx the parse tree
	 */
	void exitOrExpression(ChoralParser.OrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpression(ChoralParser.AndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpression(ChoralParser.AndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpression(ChoralParser.EqualityExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpression(ChoralParser.EqualityExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpression(ChoralParser.RelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpression(ChoralParser.RelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpression(ChoralParser.AdditiveExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpression(ChoralParser.AdditiveExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpression(ChoralParser.MultiplicativeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpression(ChoralParser.MultiplicativeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpression(ChoralParser.UnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpression(ChoralParser.UnaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ChoralParser#fwd_chain}.
	 * @param ctx the parse tree
	 */
	void enterFwd_chain(ChoralParser.Fwd_chainContext ctx);
	/**
	 * Exit a parse tree produced by {@link ChoralParser#fwd_chain}.
	 * @param ctx the parse tree
	 */
	void exitFwd_chain(ChoralParser.Fwd_chainContext ctx);
}