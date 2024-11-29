package choral.compiler.amend;


import choral.ast.CompilationUnit;
import choral.ast.ImportDeclaration;
import choral.ast.body.Class;
import choral.ast.body.ClassMethodDefinition;
import choral.ast.body.ConstructorDefinition;
import choral.ast.body.Interface;
import choral.ast.body.MethodSignature;
import choral.ast.body.VariableDeclaration;
import choral.ast.body.Enum;
import choral.ast.body.Field;
import choral.types.GroundDataType;
import choral.types.GroundInterface;
import choral.types.World;
import choral.types.Member.HigherMethod;
import choral.utils.Pair;
import choral.ast.Name;
import choral.ast.WithTypeAnnotation;
import choral.ast.type.*;
import choral.ast.visitors.AbstractChoralVisitor;
import choral.ast.statement.*;
import choral.ast.expression.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * A basic communication inference. Replace a dependency with a communication of that dependency.
 * <p>
 * For example, the code
 * <pre>
 * {@code
 * SymChannel@( A, B )<String> channel;
 *String@B b = "var_b"@B;
 *String@A a = "var_a,"@A + b;
 * }
 * </pre>
 * Would become 
 * <pre>
 * {@code
 * SymChannel@( A, B )<String> channel;
 *String@B b = "var_b"@B;
 *String@A a = "var_a,"@A + ch.<String>com( b );
 * }
 * </pre>
 */
public class BasicInference {
	/*
	 * Iterate through all dependencies
	 * Find a channel that can be used (based on typeargument)
	 * iterate through that channels com methods
	 * find a com method that can be used (based on input and output worlds)
	 * if no such method is found, throw an error
	 * replace the dependency expression with a communication of the dependency expression
	 */
	// TODO cleanup. This is a mess.
	public static CompilationUnit inferComms( CompilationUnit cu ){
		
		// a map mapping from a statement to a list of all dependency expressions within that statement
		Map<Statement, List<Pair<Expression, Expression>>> amendedStatements = new HashMap<>();
		
		for( HigherMethod method : getMethods(cu) ){
			for( Entry<World, List<Pair<Expression, Statement>>> entryset : method.worldDependencies().entrySet() ){
				
				World receiver = entryset.getKey();
				
				for( Pair<Expression, Statement> dependencyPair : entryset.getValue() ){
					Expression dependencyExpression = dependencyPair.left();
					Statement dependencyStatement = dependencyPair.right();
					List<? extends World> senders = ((GroundDataType)dependencyExpression.typeAnnotation().get()).worldArguments();
					if( senders.size() != 1 ){
						// We don't accept dependencies with multiple sender worlds
						System.out.println( "Found Dependency with " + senders.size() + "senders, expected 1" );
						return null;
						// TODO throw some exception
					}
					
					World sender = senders.get(0);
					
					System.out.println( "Role " + receiver + " needs " + dependencyExpression + " from role " + sender );
					
					Pair<Pair<String, GroundInterface>, HigherMethod> comPair = findComMethod(receiver, sender, method.channels());
					if( comPair == null ){
						// No viable communication method was found.
						System.out.println( "No viable communication method was found for the dependency " + dependencyExpression );
						// TODO throw some exception
						return null;
					}

					// The pair consists of the channel's identifier and its type.
					Pair<String, GroundInterface> comChannelPair = comPair.left();
					HigherMethod comMethod = comPair.right();

					Expression newExpression = createComExpression(dependencyExpression, comMethod, comChannelPair);

					// Put in a list to be accesable later
					amendedStatements.putIfAbsent(dependencyStatement, new ArrayList<>());
					amendedStatements.get(dependencyStatement).add(new Pair<>(dependencyExpression, newExpression));
					
					// below is just for testing, not functional
					/*
					for( GroundInterface channel : method.channels() ){
						if( channel.typeArguments().size() == 1 ){ 		// checks that this channel is not a purely selection chanel (only
																		// data channels have a type argument)
							System.out.println( "Potential channel: " + channel );
							System.out.println( "type argument check: " + channel.typeArguments().get(0).isSubtypeOf_relaxed(dependencyType) ); 
							// TODO find proper way to check if channel dependencyType is a subtype of channel.typeArguments 
							
						}
						
					} */
				}
			}
			method.clearDependencies();
		}

		// Since everything in a CompilationUnit is final (in particular Statemetns and 
		// Expressions) we need to create a new CompilationUnit

		return createNewCompilationUnit(cu, amendedStatements); 
	}

	/**
	 * Returns the first viable com method based on the input, or null is none is found. 
	 * That is a method with name "com" which takes a type at world {@code sender} and 
	 * retruns a type at world {@code receiver}.
	 * <p>
	 * TODO also check that the channel can send the type of the dependency (need to take
	 * another parameter)
	 */
	private static Pair<Pair<String, GroundInterface>, HigherMethod> findComMethod(World recepient, World sender, List<Pair<String, GroundInterface>> channels){
		for( Pair<String, GroundInterface> channelPair : channels ){
			Optional<? extends HigherMethod> comMethodOptional = 
				channelPair.right().methods()
					.filter( method ->
						method.identifier().equals("com") && // it is a com method (only checked through name)
						method.innerCallable().signature().parameters().get(0).type().worldArguments().equals(List.of(sender)) && // its parameter's worlds are equal to our dependency's world(s)
						method.innerCallable().returnType() instanceof GroundDataType && // probably redundant check, returntype should not be able to be void
						((GroundDataType)method.innerCallable().returnType()).worldArguments().get(0).equals(recepient) ) // its returntype's world is equal to our dependency recipient
					.findAny();
			
			if( comMethodOptional.isPresent() ){
				return new Pair<>( channelPair, comMethodOptional.get());
			}
		}
		return null;
	}

	/**
	 * Creates the {@code Expression} containing the communiction of the dependency.
	 * This expression needs
	 * <p>
	 * 1. a name
	 * 		- (the name of out communication method (com))
	 * <p>
	 * 2. argumetns 
	 * 		- (our dependency expression)
	 * <p>
	 * 3. type argumetns
	 * 		- com methods always need the type of the data they are communicating. 
	 * 		this is stored as a type expression. from looking at how choral treats 
	 * 		com methods in the examples, these type expressions onle have a name (and 
	 * 		NOTHING else). also this name is only the simple name for the type? e.g. 
	 * 		not "java.lang.Object", only "Object". so this is the unqualified(?) name 
	 * 		for the type.
	 */
	private static Expression createComExpression(Expression dependency, HigherMethod comMethod, Pair<String, GroundInterface> channelPair){
		GroundDataType dependencyType = ((GroundDataType)dependency.typeAnnotation().get()); // 99% certain this cannot be void, maybe add an assert?
		final List<Expression> arguments = List.of(dependency);
		final Name name = new Name(comMethod.identifier());
		final List<TypeExpression> typeArguments = List.of(new TypeExpression(new Name(dependencyType.typeConstructor().toString()), Collections.emptyList(), Collections.emptyList()));
		// TODO how do I get the type's identifier without relying on toString?
		
		MethodCallExpression scopedExpression = new MethodCallExpression(name, arguments, typeArguments, dependency.position());
		FieldAccessExpression scope = new FieldAccessExpression(new Name(channelPair.left()), dependency.position());
		// newExpression.setMethodAnnotation(comMethod.applyTo(comMethod.typeParameters()));
		// newExpression.setTypeAnnotation(comMethod.innerCallable().returnType());
		
		// below is used to compare to other com methods.
		/*System.out.println( "newExpression: " + newExpression );
		System.out.println( "MethodCallExpression: " + newExpression.toString() );
		System.out.println( "typearguments: " + newExpression.typeArguments().get(0).name() );
		System.out.println( "typearguments: " + newExpression.typeArguments().get(0).typeArguments() );
		System.out.println( "typearguments: " + newExpression.typeArguments().get(0).worldArguments() );*/
		
		return new ScopedExpression(scope, scopedExpression);
	}

	/**
	 * Retreives all methods from the {@code CompilationUnit}
	 */
	private static List<HigherMethod> getMethods( CompilationUnit cu ){
		return cu.classes().stream()
			.flatMap( cls -> cls.methods().stream() )
			.map( method -> method.signature().typeAnnotation().get() ) // we assume that methods are type-annotated
			.toList();
	}

	/**
	 * Creates a new {@code CompilationUnit} from the old, with amended method bodies (changed to 
	 * include communications) 
	 * <p>
	 * We need to create a new {@code CompilationUnit} since everything in a {@code CompilationUnit} (in 
	 * particular {@code Statements} and {@code Expressions}) are final, and can therefore not be changed.
	 */
	private static CompilationUnit createNewCompilationUnit( CompilationUnit old, Map<Statement, List<Pair<Expression, Expression>>> amendedStatements ){
		List<Class> newClasses = new ArrayList<>();
		for( Class cls : old.classes() ){
			List<ClassMethodDefinition> newMethods = new ArrayList<>();
			for( ClassMethodDefinition method : cls.methods() ){
				Statement newBody = null;
				if( method.body().isPresent() ){
					newBody = new VisitStatement(amendedStatements).visit(method.body().get());
					// newBody = method.body().get();
				}
				
				MethodSignature newMethodSignature = method.signature();
				newMethodSignature.setTypeAnnotation(method.signature().typeAnnotation().get());

				newMethods.add(new ClassMethodDefinition(
					method.signature(), 
					newBody, 
					method.annotations(), 
					method.modifiers(), 
					method.position()));
			}

			// everything that has annotations (e.g. typeannotations) need to be given these annotations explicitly
			// maybe they don't ??? TODO test this

			List<FormalTypeParameter> newTypeParameters = cls.typeParameters();
			for(int i = 0; i < newTypeParameters.size(); i++){
				FormalTypeParameter oldTypeParameter = cls.typeParameters().get(i);
				FormalTypeParameter newTypeParameter = newTypeParameters.get(i);
				if( oldTypeParameter.typeAnnotation().isPresent() ) 
					newTypeParameter.setTypeAnnotation(oldTypeParameter.typeAnnotation().get());
			}
			
			TypeExpression newExtendedClass = cls.extendsClass();
			if( cls.extendsClass() != null && cls.extendsClass().typeAnnotation().isPresent() )
				newExtendedClass.setTypeAnnotation( cls.extendsClass().typeAnnotation().get() );

			List<TypeExpression> newImplementsInterfaces = cls.implementsInterfaces();
			setAnnotations( newImplementsInterfaces, cls.implementsInterfaces() );

			List<Field> newFields = cls.fields();
			for(int i = 0; i < newFields.size(); i++){
				Field oldField = cls.fields().get(i);
				Field newField = newFields.get(i);
				if( oldField.typeAnnotation().isPresent() ) 
					newField.setTypeAnnotation(oldField.typeAnnotation().get());
			}
			
			List<ConstructorDefinition> newConstructors = cls.constructors();
			for(int i = 0; i < newConstructors.size(); i++){
				ConstructorDefinition oldConstructor = cls.constructors().get(i);
				ConstructorDefinition newConstructor = newConstructors.get(i);
				if( oldConstructor.typeAnnotation().isPresent() ) 
					newConstructor.setTypeAnnotation(oldConstructor.typeAnnotation().get());
			}


			newClasses.add(new Class(
				cls.name(), 
				cls.worldParameters(), 
				newTypeParameters, 
				newExtendedClass, 
				newImplementsInterfaces, 
				newFields, 
				newMethods, 
				newConstructors, 
				cls.annotations(), 
				cls.modifiers(), 
				cls.position()));
		}

		// everything that has annotations (e.g. typeannotations) need to be given these annotations explicitly
		// maybe they don't ??? TODO test this

		List<ImportDeclaration> newImports = old.imports();
		setAnnotations( newImports, old.imports() );
		
		List<Interface> newInterfaces = old.interfaces();
		for(int i = 0; i < newInterfaces.size(); i++){
			Interface oldInterface = old.interfaces().get(i);
			Interface newInterface = newInterfaces.get(i);
			if( oldInterface.typeAnnotation().isPresent() ) 
					newInterface.setTypeAnnotation(oldInterface.typeAnnotation().get());
		}
		
		List<Enum> newEnums = old.enums();
		for(int i = 0; i < newEnums.size(); i++){
			Enum oldEnum = old.enums().get(i);
			Enum newEnum = newEnums.get(i);
			if( oldEnum.typeAnnotation().isPresent() ) 
					newEnum.setTypeAnnotation(oldEnum.typeAnnotation().get());
		}

		return new CompilationUnit(
			old.packageDeclaration(), 
			newImports, 
			newInterfaces, 
			newClasses, 
			newEnums, 
			old.position().sourceFile());
	}


	private static <A,T extends WithTypeAnnotation<A>> void setAnnotations(List<T> newTs, List<T> oldTs){
		// Why don't everything with a typeannotation extend WithTypeAnnotation ?!?
		for(int i = 0; i < newTs.size(); i++){
			T oldT = oldTs.get(i);
			T newT = newTs.get(i);
			if( oldT.typeAnnotation().isPresent() ) newT.setTypeAnnotation(oldT.typeAnnotation().get());
		}
	}

	/*
	 * TODO
	 * since compilationunits are final we cannot replace/insert statements or expressions
	 * we ned to create a new compilationunit
	 * we create a map of all the statements cocntaining expressions that need to be amended
	 * we run through the old compilationunits method bodies
	 * for all statements
	 * 	  if they are in this list of amended statements
	 * 	     create a new statement identical to the old, except with the dependency expression 
	 * 		 replaced with the com expression
	 * 	  else
	 * 		 keep the old statement
	 * 
	 * Statement types we don't care about:
	 * 		Statement, BlockStatement, NilStatement
	 * Statement types we do care about:
	 * 		ExpressionStatement, IfStatement, TryCatchStatement (VariableDeclarations 
	 * 		have expressions), ReturnStatement, VariableDeclarationStatement
	 * Others:
	 * 		SwitchStatement (not supported)
	 */

	/**
	 * Amends {@code Statements}.
	 * <p>
	 * Iterates through {@code Statement}s and their continuations, and anytime a {@code 
	 * Statement} is in the {@code amendedStatements} map, the {@code Expression}s of that 
	 * {@code Statement} are visited by {@code VisitExpression}
	 */
	private static class VisitStatement extends AbstractChoralVisitor< Statement >{
		
		/** A map mapping Statements to a list of all dependencies within that Statement 
		 * <p>
		 * mapping form a {@code Statement} to a {@code List} of {@code Pair}s of {@code 
		 * Expression}s. Each {@code Pair} represents a dependency, where {@code Pair.right()} 
		 * is the {@code comExpression} for this dependency and {@code pair.left()} is the 
		 * original {@code Expression} of the dependency.
		 */
		Map<Statement, List<Pair<Expression, Expression>>> amendedStatements;
		
		public VisitStatement(Map<Statement, List<Pair<Expression, Expression>>> amendedStatements){
			this.amendedStatements = amendedStatements;
		}

		@Override
		public Statement visit( Statement n ) {
			return n.accept( this );
		}

		@Override
		public Statement visit( ExpressionStatement n ) {
			List<Pair<Expression, Expression>> dependencyPairList = amendedStatements.get(n);
			Expression newExpression;
			if( dependencyPairList == null ){
				// If this statement has no dependencies, there is no reason to visit its expression
				newExpression = n.expression();
			} else{
				newExpression = visitExpression(dependencyPairList, n.expression());
			}
			ExpressionStatement newStatement = new ExpressionStatement(newExpression, visitContinutation(n.continuation()), n.position());

			setReturn(newStatement, n);
			return newStatement;
		}

		@Override
		public Statement visit( VariableDeclarationStatement n ) {
			List<Pair<Expression, Expression>> dependencyPairList = amendedStatements.get(n);
			if( dependencyPairList == null ){
				// If this statement has no dependencies, there is no reason to visit its expressions (n.variables())
				VariableDeclarationStatement newStatement = new VariableDeclarationStatement(
					n.variables(), 
					visitContinutation(n.continuation()), 
					n.position());
				setReturn(newStatement, n);
				return newStatement;
			}
			List<VariableDeclaration> newVariables = new ArrayList<>();
			for( VariableDeclaration x : n.variables() ) {
				// If there are dependencies, we visit each VariableDeclaration seperately
				AssignExpression newInitializer = null;
				if( x.initializer().isPresent() ){
					Expression newInit = visitExpression(dependencyPairList, x.initializer().get());
					newInitializer = (AssignExpression)newInit;
				}

				// Create a new VariableDeclaration from the ols with the potentially amended Initializer 
				newVariables.add(new VariableDeclaration(
					x.name(), 
					x.type(), 
					x.annotations(), 
					newInitializer,
					x.position()));
			}
			VariableDeclarationStatement newStatement = new VariableDeclarationStatement(
				newVariables, 
				visitContinutation(n.continuation()), 
				n.position());
			setReturn(newStatement, n);
			return newStatement;
		}

		@Override
		public Statement visit( NilStatement n ) {
			return new NilStatement(n.position());
		}

		/**
		 * Sets the return annotation for {@code newStatement} to be equal to that of {@code oldStatement}
		 */
		private void setReturn(Statement newStatement, Statement oldStatement){
			newStatement.setReturnAnnotation(oldStatement.returnAnnotation());
			if( oldStatement.returns() ) newStatement.setReturns();
		}

		/** 
		 * Visits the continuation if there is one 
		 */
		private Statement visitContinutation( Statement continutation ){
			return continutation == null ? null : visit(continutation);
		}

		/**
		 * For some initial {@code Expression} and a list of {@code dependencyPair}s, visits the 
		 * initial {@code Expression} once for each dependencyPair.
		 * @param dependencyPairList must not be empty
		 */
		private Expression visitExpression( List<Pair<Expression, Expression>> dependencyPairList, Expression first ){
			Expression newExpression = new VisitExpression(dependencyPairList.get(0)).visit(first);
			
			for( int i = 1; i < dependencyPairList.size(); i++ ){
				Pair<Expression, Expression> dependencyPair = dependencyPairList.get(i);
				newExpression = new VisitExpression(dependencyPair).visit(newExpression);
			}
			
			return newExpression;
		}

	}

	/**
	 * Amends {@code Expressions}.
	 * <p>
	 * Iterates through {@code Expression}s and checks if they are equal to {@code originalExpression}.
	 * If they are they are replaced with {@code comExpression}.
	 */
	private static class VisitExpression extends AbstractChoralVisitor< Expression >{
		/*
		 * TODO
		 * comExpressions need to be made on the fly.
		 * This is because we might have nested dependencies.
		 * for example, consider the expression (taken from the simplemethodcalls example)
		 * c_A.fun_in( c_B.fun_in_out( c_A.fun_out() ) );
		 * where 
		 * 		c_A.fun_in() takes a Integer@A, 
		 * 		c_B.fun_in_out() takes an Integer@B and retruns an Integer@B
		 * 		c_A.fun_out() returns an Integer@A
		 * we have two dependencies:
		 * 		c_A.fun_in() needs c_B.fun_in_out()
		 * 		c_B.fun_in_out() needs c_A.fun_out()
		 * we need to make sure that the outer dependency (c_A.fun_in() needs c_B.fun_in_out()) 
		 * contains the inner dependency (c_B.fun_in_out() needs c_A.fun_out())
		 * 
		 * It might be the case that we can simply solve the outer dependency first folowed by 
		 * the inner dependency.
		 * We cannot solve the inner solution first, since after this is solved, the outer dependency 
		 * doesn't match the original problematic expression anymore.
		 * 
		 * also TODO support more expressions
		 */

		/** The communication expression to be inserted into the AST */
		Expression comExpression;
		/** The problematic expression */
		Expression originalExpression;
		

		public VisitExpression(Pair<Expression, Expression> dependencyPair){
			this.comExpression = dependencyPair.right();
			this.originalExpression = dependencyPair.left();
		}

		@Override
		public Expression visit( Expression n ) {
			return n.accept( this );
		}

		@Override
		public Expression visit( ScopedExpression n ) {
			Expression newscopedExpression;
			if( n.scopedExpression().equals(originalExpression) ){
				newscopedExpression = comExpression;
			}else{
				newscopedExpression = visit(n.scopedExpression());
			}

			// Dependencies cannot be in the scope, so no need to check
			Expression newScope = n.scope(); 
			ScopedExpression newScopedExpression = new ScopedExpression(newScope, newscopedExpression, n.position());

			return newScopedExpression;
		}

		@Override
		public Expression visit( FieldAccessExpression n ) {
			if( n.equals(originalExpression) ){
				return comExpression;
			}else{
				return n;
			}
		}

		@Override
		public Expression visit( MethodCallExpression n ) {
			List<Expression> newArgs = new ArrayList<>();

			for(int i = 0; i < n.arguments().size(); i++){
				Expression argument = n.arguments().get(i);
				
				// check if any of the argumetns are equal to the originalExpression
				if( argument.equals(originalExpression) ){
					newArgs.add(comExpression); 
					// We might be able to return quickly if we when we find the originalExpression
					// for now we don't TODO implement quicker returns
				}else{
					// otherwise visit the argument
					newArgs.add( visit(argument) );
					// same here, if !visit(argument).equals(argument) then the argument must have 
					// been amended, and thus the originalExpression must have been found in this 
					// argument and we should be able to return quickly.
				}

			}
			MethodCallExpression newMethodCallExpression = new MethodCallExpression(n.name(), newArgs, n.typeArguments(), n.position());
			if( n.typeAnnotation().isPresent() ) newMethodCallExpression.setTypeAnnotation(n.typeAnnotation().get());
			if( n.methodAnnotation().isPresent() ) newMethodCallExpression.setMethodAnnotation(n.methodAnnotation().get());
			return newMethodCallExpression;
		}
		
		@Override
		public Expression visit( AssignExpression n ) {
			Expression newValue;
			if( n.value().equals(originalExpression) ){
				newValue = comExpression;
			}else{
				newValue = visit(n.value());
			}
			Expression newTarget = n.target(); // the dependency cannot be part of the target
			newTarget.setTypeAnnotation(n.target().typeAnnotation().get());
			AssignExpression newAssignExpression = new AssignExpression(newValue, newTarget, n.operator());
			newAssignExpression.setTypeAnnotation(n.typeAnnotation().get());

			return newAssignExpression;
		}


	}

}
