package choral.compiler.amend.MiniZincInference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import choral.ast.CompilationUnit;
import choral.ast.Name;
import choral.ast.body.VariableDeclaration;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.AbstractChoralVisitor;
import choral.compiler.amend.Utils;
import choral.compiler.merge.ExpressionsMerger;
import choral.exceptions.CommunicationInferenceException;
import choral.types.GroundClass;
import choral.types.GroundClassOrInterface;
import choral.types.GroundDataType;
import choral.types.GroundDataTypeOrVoid;
import choral.types.GroundInterface;
import choral.types.GroundReferenceType;
import choral.types.GroundTypeParameter;
import choral.types.Universe;
import choral.types.Member.HigherCallable;
import choral.types.Member.HigherMethod;
import choral.types.World;
import choral.utils.Pair;

public class GenerateMiniZincInputs {

	Map<HigherCallable, MiniZincInput> allInputs = new HashMap<>();

    public GenerateMiniZincInputs(){}

    public Map<HigherCallable, MiniZincInput> generateInputs( CompilationUnit cu ){
		
		// Generate the MiniZinc inputs from the CompilationUnit
		buildMiniZincInput(cu);

		return allInputs;
	}

    /** 
	 * Generates a MiniZinc input for all the methods in the CompilationUnit. These inputs are
	 * stored in the allInputs map.
	 */
	private void buildMiniZincInput( CompilationUnit cu ){
		for( Pair<HigherCallable, Statement> methodPair : Utils.getMethods(cu) ){
			HigherCallable method = methodPair.left();
			Statement methodStatement = methodPair.right();

			//* maps an expression to the index of the dependency it belongs to */
			Map<Expression, Integer> dependencyExpressions = new HashMap<>();

			MiniZincInput input = new MiniZincInput();
			
			for( Entry<World, List<Pair<Expression, Statement>>> dependencySet : method.worldDependencies().entrySet() ){    
				World recipient = dependencySet.getKey();
				
				for( Pair<Expression, Statement> dependencyPair : dependencySet.getValue() ){
					Expression dependencyExpression = dependencyPair.left();

					// Extract sender from dependency (what world needs to send data)
					World sender = getSender(dependencyExpression);

					// create Dependency object and find a valid channel
					MiniZincInput.Dependency dependency = new MiniZincInput.Dependency(dependencyExpression, sender, recipient);
					dependency.setComMethod(method.channels());

					
                    // add dependency to MiniZinc input
					// unless the dependency already exists
					if( !isDuplicateDependency( dependencyExpression, dependencyExpressions ) ){
						input.num_deps ++;
						input.dependencies.add(dependency);
						dependencyExpressions.put(dependencyExpression, input.num_deps);
						input.dependencyStrings.add(dependencyExpression.toString());
						input.dep_from.add(sender);
						input.dep_to.add(recipient);
						input.dep_def_at.add( 0 );
					}
                    
				}
			}
			

			// Add everything else to the MiniZinc input
			input.num_blocks ++;
			input.blocks.add(new MiniZincInput.Block(0, 0, 0)); // global block

			new VisitStatement( input, dependencyExpressions ).visitContinutation(methodStatement); // visit method body
			input.blocks.get(0).end = input.in_size; // set end of global block

			List<World> worlds = cu.classes().stream() // collect all roles in method
				.flatMap( cls -> cls.worldParameters().stream() )
				.map( formalWorld -> new World( new Universe(), formalWorld.name().identifier() ) )
				.toList();
			input.roles = worlds;

			allInputs.put( method, input );
		}
	}

	/**
	 * Checks whether or not the target expression is already a dependency.
	 */
	private boolean isDuplicateDependency( 
		Expression target, 
		Map<Expression, Integer> allExpressions
	){
		for( Expression otherExpression : allExpressions.keySet() ){
			// try to merge target with otherExpression. If success, then target is a duplicate of 
			// otherExpression. If failure, then target is not a duplicate of otherExpression
			try { 
				ExpressionsMerger.mergeExpressions(target, otherExpression);

				// add target to allExpressions and map it to the dependency it is a duplicate of
				allExpressions.put(target, allExpressions.get(otherExpression));
				return true;

			} catch (Exception e) { }
		}
		// if all merges fail then target is not a duplicate
		return false;
	}

    /**
	 * Returns the world from a dependency expression that needs to send data. If the 
	 * dependency expression has more than one sender world an error is thrown.
	 * <p>
	 * If the dependency expression is a method call, the type-annotation is set to 
	 * be equal to the method annotation.
	 */
	private World getSender( Expression dependencyExpression ){
		// Extract senders from dependency (what world(s) needs to send data)
		List<? extends World> senders;
		if( dependencyExpression instanceof MethodCallExpression ){
			// MethodCallExpressions don't use typeAnnotation but instead use methodAnnotation
			GroundDataType methodReturnType = (GroundDataType)((MethodCallExpression)dependencyExpression).methodAnnotation().get().returnType();
			// Set typeannotation = returntype here for more easy access to an expression's type
			dependencyExpression.setTypeAnnotation(methodReturnType); 
			senders = methodReturnType.worldArguments();
		} else {
			senders = ((GroundDataType)dependencyExpression.typeAnnotation().get()).worldArguments();
		}
		
		if( senders.size() != 1 ){
			// We don't accept dependencies with multiple sender worlds
			throw new CommunicationInferenceException( "Found Dependency with " + senders.size() + " senders, expected 1" );
		}
		return senders.get(0);
	}

	private class Dependency {
		private Expression originalExpression;
		private HigherMethod comMethod;
		private String channelIdentifier;
		private GroundInterface channel;
		private GroundDataType type;
        private World sender;
        private World recipient;

		public Dependency( Expression originalExpression, World sender, World recipient ){
			this.originalExpression = originalExpression;
			GroundDataTypeOrVoid t = originalExpression.typeAnnotation().get();
			if( t.isVoid() )
				throw new CommunicationInferenceException( "Dependency cannot be of type void: " + originalExpression );
			this.type = (GroundDataType)t;
			this.recipient = recipient;
			this.sender = sender;
		}

		public Expression originalExpression(){
			return originalExpression;
		}

		public GroundDataType type(){
			return type;
		}

		public HigherMethod comMethod(){
			return comMethod;
		}

		public String channelIdentifier(){
			return channelIdentifier;
		}

		public GroundInterface channel(){
			return channel;
		}

        public World recipient(){
			return recipient;
		}

		public World sender(){
			return sender;
		}

		public void setChannel( Pair<String, GroundInterface> channelPair ){
			this.channelIdentifier = channelPair.left();
			this.channel = channelPair.right();
		}

		public void setComMethod( HigherMethod comMethod ){
			this.comMethod = comMethod;
		}

		/**
		 * Creates the {@code Expression} containing the communiction of the dependency. Note
		 * that the argument of the communication method ({@code visitedExpression}) must 
		 * be visited before creating the comExpression.
		 * <p>
		 * This expression needs
		 * <p>
		 * 1. A name
		 * 		- The name of out communication method (com)
		 * <p>
		 * 2. Arguments 
		 * 		- Our {@code visitedDependency} expression. This is expected to be a 
		 * 		visited version of {@code originalExpression}. Note that this must be 
		 * 		visited before calling {@code createComExpression}. This is because 
		 * 		we use java's {@code Object.equals()} to check if  an expression is a 
		 * 		dependency. If {@code createComExpression} is called before visiting 
		 * 		{@code originalExpression} then dependencies inside
		 * 		{@code originalExpression} (nested dependencies) will not be caught. 
		 * <p>
		 * 3. type argumetns
		 * 		- com methods always need the type of the data they are communicating. 
		 * 		This is stored as a {@code TypeExpression}. These TypeExpressions contain
		 * 		the unqualified name of the type (e.g. not "java.lang.Object", only 
		 * 		"Object") and composite types (types containing other types (like 
		 * 		{@code List})) also have a list of {@code TypeExpression}s representing 
		 * 		its inner types.
		 * @param visitedDependency - Must be visited before calling this method, 
		 * 		otherwise nested dependencies will not be caught.
		 */
		public Expression createComExpression( Expression visitedDependency ){

			TypeExpression typeExpression;
            if( originalExpression.typeAnnotation().get() instanceof GroundTypeParameter ){
				typeExpression = getTypeExpression((GroundTypeParameter)originalExpression.typeAnnotation().get());
            } else{ 
				typeExpression = getTypeExpression((GroundClassOrInterface)originalExpression.typeAnnotation().get());
			}

			final List<Expression> arguments = List.of( visitedDependency );
			final Name name = new Name(comMethod.identifier());
			final List<TypeExpression> typeArguments = List.of( typeExpression );
			
			MethodCallExpression scopedExpression = new MethodCallExpression(name, arguments, typeArguments, visitedDependency.position());
			
			// The comMethod is a method inside a channel, so we need to make the channel its scope
			FieldAccessExpression scope = new FieldAccessExpression(new Name(channelIdentifier), visitedDependency.position());
			
			// Something like channel.< Type >com( visitedDependency )
			return new ScopedExpression(scope, scopedExpression);
		}

		private TypeExpression getTypeExpression( GroundClassOrInterface type ){
			return new TypeExpression(
				new Name(type.typeConstructor().identifier()),
				Collections.emptyList(), 
				type.typeArguments().stream().map( typeArg -> getTypeExpression(typeArg.applyTo(type.worldArguments())) ).toList());
		}

		private TypeExpression getTypeExpression( GroundReferenceType type ){
			if( type instanceof GroundClass ){ // I think this is only not true for primitive types, which cannot be communicated
				GroundClass typeGC = (GroundClass)type;
				return new TypeExpression(
					new Name(typeGC.typeConstructor().identifier()),
					Collections.emptyList(), 
					typeGC.typeArguments().stream().map( typeArg -> getTypeExpression(typeArg.applyTo(type.worldArguments())) ).toList());
			}
			if( type instanceof GroundTypeParameter ){
				GroundTypeParameter typeGTP = (GroundTypeParameter)type;
				return new TypeExpression(
					new Name(typeGTP.typeConstructor().identifier()),
					Collections.emptyList(), 
					Collections.emptyList());
			}
			
			throw new CommunicationInferenceException( "ERROR! Not a GroundClass or GroundTypeParameter. Found " + type.getClass() ); 
		}

        /**
         * returns the type of the dependency's original expression as a TypeExpression
         * @return
         */
        public TypeExpression getType(){
            if( originalExpression.typeAnnotation().get() instanceof GroundTypeParameter ){
				return getType((GroundTypeParameter)originalExpression.typeAnnotation().get());
            } else{ 
				return getType((GroundClassOrInterface)originalExpression.typeAnnotation().get());
			}
        }

        private TypeExpression getType( GroundClassOrInterface type ){
            return new TypeExpression(
				new Name(type.typeConstructor().identifier()),
				List.of( new WorldArgument(new Name(recipient.identifier()), null) ), 
				type.typeArguments().stream().map( typeArg -> getTypeExpression(typeArg.applyTo(type.worldArguments())) ).toList());
		}

		private TypeExpression getType( GroundReferenceType type ){
            if( type instanceof GroundClass ){ // I think this is only not true for primitive types, which cannot be communicated
				GroundClass typeGC = (GroundClass)type;
				return new TypeExpression(
					new Name(typeGC.typeConstructor().identifier()),
					List.of( new WorldArgument(new Name(recipient.identifier()), null) ), 
					typeGC.typeArguments().stream().map( typeArg -> getTypeExpression(typeArg.applyTo(type.worldArguments())) ).toList());
			}
			if( type instanceof GroundTypeParameter ){
				GroundTypeParameter typeGTP = (GroundTypeParameter)type;
				return new TypeExpression(
					new Name(typeGTP.typeConstructor().identifier()),
					List.of( new WorldArgument(new Name(recipient.identifier()), null) ), 
					Collections.emptyList());
			}
			
			throw new CommunicationInferenceException( "ERROR! Not a GroundClass or GroundTypeParameter. Found " + type.getClass() ); 
		}

	}

	/**
	 * Populates the given MiniZinc input. Also visits all expressions.
	 */
	private class VisitStatement extends AbstractChoralVisitor< Void >{
		
		MiniZincInput input;
		int currentBlock;
		Map< Expression, Integer > dependencyExpressions;
		Map< String, Integer > variableDefinedAt = new HashMap<>();
		
		public VisitStatement( MiniZincInput input, Map< Expression, Integer > dependencyExpressions ){
			this.input = input;
			this.currentBlock = input.num_blocks;
			this.dependencyExpressions = dependencyExpressions;
		}

		@Override
		public Void visit( Statement n ) {
			return n.accept( this );
		}

		@Override
		public Void visit( ExpressionStatement n ) {
			input.in_size ++;
			input.statements.add(n.expression().toString());
			input.statements_blocks.add(currentBlock);
			input.statements_roles.add(getWorld(n.expression()));
			input.statementIndices.put(n, List.of(input.in_size));

			
			visitExpression(n.expression(), n);
			
			return visitContinutation(n.continuation());
		}

		@Override
		public Void visit( VariableDeclarationStatement n ) {
			List<Integer> indices = new ArrayList<>();
			
			for( VariableDeclaration v : n.variables() ){
				visitVariableDeclaration( v, n );
				indices.add(input.in_size);
			}
			input.statementIndices.put(n, indices);
			return visitContinutation(n.continuation());
		}

		@Override
		public Void visit( NilStatement n ) {
			return visitContinutation(n.continuation());
		}

		@Override
		public Void visit( BlockStatement n ) {
			List<Integer> indices = new ArrayList<>();
			input.in_size ++;
			input.statements.add("{");
			input.statements_blocks.add(currentBlock);
			input.statements_roles.add(null);
			indices.add(input.in_size);

			int block_start = input.in_size;
			int block_parent = currentBlock;

			input.num_blocks ++;
			currentBlock = input.num_blocks;

			visitContinutation(n.enclosedStatement());

			input.in_size ++;
			input.statements.add("}");
			input.statements_blocks.add(currentBlock);
			input.statements_roles.add(null);
			indices.add(input.in_size);

			input.blocks.add(new MiniZincInput.Block(block_start, input.in_size, block_parent));
			currentBlock = block_parent;
			input.statementIndices.put(n, indices);

			return visitContinutation(n.continuation());
		}

		@Override
		public Void visit( IfStatement n ) {
			List<Integer> indices = new ArrayList<>();

			// condition
			input.in_size ++;
			input.statements.add( "if(" + n.condition() + "){" );
			input.statements_blocks.add(currentBlock);
			World if_role = ((GroundDataType)n.condition().typeAnnotation().get()).worldArguments().get(0);
			input.statements_roles.add( if_role );
			indices.add(input.in_size);

			visitExpression(n.condition(), n);
			
			int if_parent = currentBlock;

			// if-branch
			int if_start = input.in_size;

			input.num_blocks ++;
			int then_block = input.num_blocks;
			currentBlock = then_block;
			// inserting the block into the list to make sure the index of the then-block matches "then_block"
			input.blocks.add(new MiniZincInput.Block( if_start, 0, if_parent )); // the end of the block will be set later

			visitContinutation(n.ifBranch());

			// adding the "} else {"
			input.in_size ++;
			input.statements.add( "} else {" );
			input.statements_blocks.add(currentBlock);
			input.statements_roles.add(null);
			indices.add(input.in_size);

			input.blocks.get(then_block-1).end = input.in_size; // set the end of the then-block

			// else-branch
			int else_start = input.in_size;

			input.num_blocks ++;
			int else_block = input.num_blocks;
			currentBlock = else_block;
			// inserting the block into the list to make sure the index of the else-block matches "else_block"
			input.blocks.add(new MiniZincInput.Block(else_start, 0, if_parent)); // the end of the block will be set later

			visitContinutation(n.elseBranch());

			// adding the final "}"
			input.in_size ++;
			input.statements.add("}");
			input.statements_blocks.add(currentBlock);
			input.statements_roles.add(null);
			indices.add(input.in_size);

			input.blocks.get(else_block-1).end = input.in_size; // set the end of the else-block
			currentBlock = if_parent;

			input.num_ifs ++;
			input.if_roles.add(if_role);
			input.if_blocks.add(new Pair<>(then_block, else_block));
			input.statementIndices.put(n, indices);
			return visitContinutation(n.continuation());
		}

		@Override // not supported
		public Void visit( SwitchStatement n ) {
			throw new UnsupportedOperationException("SwitchStatement not supported\n\tStatement at " + n.position().toString());
		}

		@Override // not supported
		public Void visit( TryCatchStatement n ) {
			throw new UnsupportedOperationException("TryCatchStatement not supported\n\tStatement at " + n.position().toString());
		}

		@Override
		public Void visit( ReturnStatement n ) {
			input.in_size ++;
			input.statements.add( "return " + n.returnExpression().toString() );
			input.statements_blocks.add(currentBlock);
			
			input.statements_roles.add(((GroundDataType)n.returnExpression().typeAnnotation().get()).worldArguments().get(0));

			input.statementIndices.put(n, List.of(input.in_size));
			
			visitExpression(n.returnExpression(), n);
			return visitContinutation(n.continuation());
		}

		/** 
		 * Visits the continuation if there is one 
		 */
		private Void visitContinutation( Statement continutation ){
			return continutation == null ? null : visit(continutation);
		}

		/**
		 * Calls {@code VisitExpression} on the given {@Expression}
		 */
		private void visitExpression( Expression expression, Statement n ){
			
			new VisitExpression( input, dependencyExpressions, variableDefinedAt ).checkIfDependency(expression);
		}

		/**
		 * Insert a variable declaration into the MiniZinc input. Also visits the initializer 
		 * if there is one.
		 */
		private void visitVariableDeclaration( VariableDeclaration vd, Statement n ){
			
			String variable = vd.type().typeAnnotation().get() + " " + vd.name();
			input.in_size ++;
			
			input.statements_blocks.add(currentBlock);
			input.statements_roles.add(new World( new Universe(), vd.type().worldArguments().get(0).name().identifier() ));

			if( !vd.initializer().isPresent() ){
				input.statements.add( variable );
				return;
			}
			input.statements.add( variable + " = " + vd.initializer().get() );
			variableDefinedAt.put(vd.name().identifier(), input.in_size);
			
			visitExpression(vd.initializer().get(), n);
		}

		/**
		 * Returns the homeworld of an expression.
		 */
		private World getWorld( Expression e ){
			World w;
			if( e instanceof MethodCallExpression ){
				MethodCallExpression me = (MethodCallExpression) e;
				w = me.methodAnnotation().get().higherCallable().declarationContext().worldArguments().get(0);
			} else if( e instanceof ScopedExpression ) {
				ScopedExpression s = (ScopedExpression)e;
				w = getWorld( s.scopedExpression() );
			} else {
				GroundDataType typeAnnotation = (GroundDataType)e.typeAnnotation().get();
				w = typeAnnotation.worldArguments().get(0);
			}
			System.out.println( "World for expression " + e  + " : " + w );
			return w;
		}
	}

	/**
	 * Checks for nested dependencies and usage of variables. Adds to {@code dep_used_at} and 
	 * sets {@code dep_def_at} in the {@code MiniZincInput}.
	 * <p>
	 * Should called through {@code checkIfDependency()}
	 */
	private class VisitExpression extends AbstractChoralVisitor< Void >{
		
		/** Maps variable identifiers to the statement in which they are defined */
		Map<String, Integer> variableDefinedAt;
		MiniZincInput input;
		/** If an expression is a dependency, this map maps it to that dependency's index in the input */
		Map< Expression, Integer > dependencyExpressions;
		/** The current innermost dependency */
		Expression innerDependency = null;

		public VisitExpression( 
			MiniZincInput input, 
			Map< Expression, Integer > dependencyExpressions,
			Map< String, Integer > variableDefinedAt
		){
			this.input = input;
			this.dependencyExpressions = dependencyExpressions;
			this.variableDefinedAt = variableDefinedAt;
		}

		/**
		 * Checks if the given Expression is a dependency, then visits that Expression
		 */
		public Void checkIfDependency( Expression n ){
			Integer dependency = dependencyExpressions.get(n);
			if( dependency == null ){
				visit(n);
			} else{
				Expression parentDependency = innerDependency;
				if( innerDependency == null ){
					MiniZincInput.Dep_use dep = new MiniZincInput.Dep_use(dependencyExpressions.get(n));
					dep.used_at = input.in_size;
					input.dep_used_at.add(dep);
				} else {
					MiniZincInput.Dep_use dep = new MiniZincInput.Dep_use(dependencyExpressions.get(n));
					dep.nested_dependency = true;
					dep.used_at = dependencyExpressions.get(innerDependency);
					input.dep_used_at.add(dep);
				}
				innerDependency = n;
				visit(n);
				innerDependency = parentDependency;
			}

			return null;
		}

		@Override
		public Void visit( Expression n ) {
			return n.accept( this );
		}

		@Override
		public Void visit( ScopedExpression n ) {
			checkIfDependency(n.scope());
			checkIfDependency(n.scopedExpression());
			
			return null;
		}

		@Override
		public Void visit( FieldAccessExpression n ) {
			// If we are inside a dependency, then we need to set dep_def_at
			if( innerDependency != null ){
				Integer definedAt = variableDefinedAt.get(n.name().identifier());
				if( definedAt != null )
					input.dep_def_at.set(dependencyExpressions.get(innerDependency)-1, definedAt);
			}
			
			return null;
		}

		@Override
		public Void visit( MethodCallExpression n ) {
			for( Expression e : n.arguments() )
				checkIfDependency(e);
			return null;
		}
		
		@Override
		public Void visit( AssignExpression n ) {
			checkIfDependency(n.value());
			return null;
		}

		@Override
		public Void visit( BinaryExpression n ) {
			checkIfDependency(n.left());
			checkIfDependency(n.right());
			return null;
		}

		@Override
		public Void visit( EnclosedExpression n ) {
			checkIfDependency(n.nestedExpression());
			return null;
		}
		
		@Override
		public Void visit( StaticAccessExpression n ) {
			return null;
		}

		@Override
		public Void visit( ClassInstantiationExpression n ) {
			for( Expression e : n.arguments() )
				checkIfDependency(e);
			return null;
		}

		@Override
		public Void visit( NotExpression n ) {
			checkIfDependency(n.expression());
			return null;
		}

		@Override // not supported
		public Void visit( ThisExpression n ) {
			throw new UnsupportedOperationException("ThisExpression not supported\n\tExpression at " + n.position().toString());
		}

		@Override // not supported
		public Void visit( SuperExpression n ) {
			throw new UnsupportedOperationException("SuperExpression not supported\n\tExpression at " + n.position().toString());
		}

		@Override
		public Void visit( NullExpression n ) {
			return null;
		}

		public Void visit( LiteralExpression.BooleanLiteralExpression n ) {
			// literals are not permited to be in dependencies
			return null;
		}

		public Void visit( LiteralExpression.IntegerLiteralExpression n ) {
			// literals are not permited to be in dependencies
			return null;
		}

		public Void visit( LiteralExpression.DoubleLiteralExpression n ) {
			// literals are not permited to be in dependencies
			return null;
		}

		public Void visit( LiteralExpression.StringLiteralExpression n ) {
			// literals are not permited to be in dependencies
			return null;
		}

		@Override // not supported
		public Void visit( TypeExpression n ) {
			throw new UnsupportedOperationException("TypeExpression not supported\n\tExpression at " + n.position().toString());
		}

		@Override // not supported
		public Void visit( BlankExpression n ){
			throw new UnsupportedOperationException("BlankExpression not supported\n\tExpression at " + n.position().toString());
		}

		@Override // not supported
		public Void visit( EnumCaseInstantiationExpression n ){
			throw new UnsupportedOperationException("EnumCaseInstantiationExpression not supported\n\tExpression at " + n.position().toString());
		}
	}

}
