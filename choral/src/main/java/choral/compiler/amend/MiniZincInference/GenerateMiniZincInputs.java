package choral.compiler.amend.MiniZincInference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import choral.ast.CompilationUnit;
import choral.ast.Name;
import choral.ast.body.VariableDeclaration;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.AbstractChoralVisitor;
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

    public Map<HigherCallable, MiniZincInput> inferComms( CompilationUnit cu ){
		
		// Generate the MiniZinc inputs from the CompilationUnit
		buildMiniZincInput(cu);

		return allInputs;
	}

    
	private void buildMiniZincInput( CompilationUnit cu ){
		for( Pair<HigherCallable, Statement> methodPair : getMethods(cu) ){
			HigherCallable method = methodPair.left();
			Statement methodStatement = methodPair.right();
			//* maps an expression to the index of the dependency it belongs to */
			Map<Expression, Integer> dependencyExpressions = new HashMap<>();
			Map<Integer, Dependency> dependencyMap = new HashMap<>();

			MiniZincInput input = new MiniZincInput();
			
			for( Entry<World, List<Pair<Expression, Statement>>> entryset : method.worldDependencies().entrySet() ){    
				World recipient = entryset.getKey();
				
				for( Pair<Expression, Statement> dependencyPair : entryset.getValue() ){
					Expression dependencyExpression = dependencyPair.left();

					// Extract sender from dependency (what world needs to send data)
					World sender = getSender(dependencyExpression);

					Dependency dependency = new Dependency(dependencyExpression, sender, recipient);
					setComMethod(dependency, method.channels());

					
                    // add dependency to MiniZinc input
					if( !isDuplicateDependency( dependencyExpression, dependencyExpressions ) ){
						input.num_deps ++;
						dependencyExpressions.put(dependencyExpression, input.num_deps);
						input.dependencies.add(dependencyExpression.toString());
						input.dep_from.add(sender);
						input.dep_to.add(recipient);
						input.dep_def_at.add( 0 );
					} 
					dependencyMap.put(input.num_deps, dependency);
                    
				}
			}
			

			// Add everything else to the MiniZinc input
			input.num_blocks ++;
			input.blocks.add(new MiniZincInput.Block(0, 0, 0)); // global block
			System.out.println( "dependencyExpressions: " + dependencyExpressions );
			new VisitStatement( input, dependencyExpressions ).visitContinutation(methodStatement); // visit method bodt
			input.blocks.get(0).end = input.in_size;
			List<World> worlds = cu.classes().stream() // collect all roles in method
				.flatMap( cls -> cls.worldParameters().stream() )
				.map( formalWorld -> new World( new Universe(), formalWorld.name().identifier() ) )
				.toList();
			input.roles = worlds;

			allInputs.put( method, input );
		}
	}

	private boolean isDuplicateDependency( 
		Expression target, Map<Expression, 
		Integer> allExpressions
	){
		for( Expression otherExpression : allExpressions.keySet() ){
			try {
				ExpressionsMerger.mergeExpressions(target, otherExpression);

				System.out.println( "Expression: " + target + " is a duplicate" );
				allExpressions.put(target, allExpressions.get(otherExpression));
				return true;

			} catch (Exception e) {
				
			}
		}
		System.out.println( "Expression: " + target + " is NOT a duplicate" );
		return false;
	}

    /**
	 * Retreives all methods from the {@code CompilationUnit} including constructors
	 */
	private List<Pair<HigherCallable, Statement>> getMethods( CompilationUnit cu ){
		return Stream.concat( 
			cu.classes().stream()
				.flatMap( cls -> cls.methods().stream() )
				.map( method -> 
					new Pair<HigherCallable, Statement>(
						method.signature().typeAnnotation().get(), // we assume that methods are type-annotated
						method.body().orElse(null)) ), 
			cu.classes().stream()
				.flatMap(cls -> cls.constructors().stream()
				.map( method -> 
					new Pair<HigherCallable, Statement>(
						method.signature().typeAnnotation().get(), 
						method.blockStatements()) )
				)).toList();
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

    
	private void setComMethod(
		Dependency dependency, 
		List<Pair<String, GroundInterface>> channels
	){
		World sender = dependency.sender();
		World recepient = dependency.recipient();
		GroundDataType type = dependency.type();
		
		for( Pair<String, GroundInterface> channelPair : channels ){

			// Data channels might not return the same datatype at the receiver as 
			// the datatype from the sender. Since we only store one type for the 
			// dependency we assume that all types in a channel are the same.
			GroundInterface channel = channelPair.right();
			if( channel.typeArguments().stream().anyMatch( typeArg -> type.typeConstructor().isSubtypeOf( typeArg ) ) ){
				
				Optional<? extends HigherMethod> comMethodOptional = 
					channelPair.right().methods()
						.filter( method ->
							method.identifier().equals("com") && // it is a com method (only checked through name)
							method.innerCallable().signature().parameters().get(0).type().worldArguments().equals(List.of(sender)) && // its parameter's worlds are equal to our dependency's world(s)
							method.innerCallable().returnType() instanceof GroundDataType && // probably redundant check, returntype should not be able to be void
							((GroundDataType)method.innerCallable().returnType()).worldArguments().get(0).equals(recepient) ) // its returntype's world is equal to our dependency recipient
						.findAny();
				
				if( comMethodOptional.isPresent() ){
					dependency.setChannel( channelPair );
					dependency.setComMethod( comMethodOptional.get() );
					return;
				}
			}
		}
		throw new CommunicationInferenceException( "No viable communication method was found for the dependency " + dependency.originalExpression() );
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

			
			visitExpression(n.expression(), n);
			
			return visitContinutation(n.continuation());
		}

		@Override
		public Void visit( VariableDeclarationStatement n ) {
			
			for( VariableDeclaration v : n.variables() ){
				visitVariableDeclaration( v, n );
			}
			return visitContinutation(n.continuation());
		}

		@Override
		public Void visit( NilStatement n ) {
			return visitContinutation(n.continuation());
		}

		@Override
		public Void visit( BlockStatement n ) {
			input.in_size ++;
			input.statements.add("{");
			input.statements_blocks.add(currentBlock);
			input.statements_roles.add(null);

			int block_start = input.in_size;
			int block_parent = currentBlock;

			input.num_blocks ++;
			currentBlock = input.num_blocks;

			visitContinutation(n.enclosedStatement());

			input.in_size ++;
			input.statements.add("}");
			input.statements_blocks.add(currentBlock);
			input.statements_roles.add(null);

			input.blocks.add(new MiniZincInput.Block(block_start, input.in_size, block_parent));
			currentBlock = block_parent;

			return visitContinutation(n.continuation());
		}

		@Override
		public Void visit( IfStatement n ) {
			System.out.println( "if start num_block: " + input.num_blocks );
			input.in_size ++;
			input.statements.add( "if(" + n.condition() + "){" );
			input.statements_blocks.add(currentBlock);
			World if_role = ((GroundDataType)n.condition().typeAnnotation().get()).worldArguments().get(0);
			input.statements_roles.add( if_role );

			visitExpression(n.condition(), n);

			int if_start = input.in_size;
			int if_parent = currentBlock;

			input.num_blocks ++;
			int then_block = input.num_blocks;
			currentBlock = then_block;
			input.blocks.add(new MiniZincInput.Block( if_start, 0, if_parent ));

			visitContinutation(n.ifBranch());

			input.in_size ++;
			input.statements.add( "} else {" );
			input.statements_blocks.add(currentBlock);
			input.statements_roles.add(null);

			input.blocks.get(then_block-1).end = input.in_size;

			int else_start = input.in_size;

			input.num_blocks ++;
			int else_block = input.num_blocks;
			currentBlock = else_block;
			input.blocks.add(new MiniZincInput.Block(else_start, 0, if_parent));

			visitContinutation(n.elseBranch());

			input.in_size ++;
			input.statements.add("}");
			input.statements_blocks.add(currentBlock);
			input.statements_roles.add(null);

			input.blocks.get(else_block-1).end = input.in_size;
			currentBlock = if_parent;

			input.num_ifs ++;
			input.if_roles.add(if_role);
			input.if_blocks.add(new Pair<>(then_block, else_block));

			System.out.println( "if end num_block: " + input.num_blocks );
			return visitContinutation(n.continuation());
		}

		@Override // not supported
		public Void visit( SwitchStatement n ) {
			throw new UnsupportedOperationException("SwitchStatement not supported\n\tStatement at " + n.position().toString());
		}

		@Override
		public Void visit( TryCatchStatement n ) {
			return visitContinutation(n.continuation());
		}

		@Override
		public Void visit( ReturnStatement n ) {
			input.in_size ++;
			input.statements.add( "return " + n.returnExpression().toString() );
			input.statements_blocks.add(currentBlock);
			
			input.statements_roles.add(((GroundDataType)n.returnExpression().typeAnnotation().get()).worldArguments().get(0));
			
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
			
			new VisitExpression( input, dependencyExpressions, variableDefinedAt ).visit(expression);
		}

		/**
		 * If there is no initializer, return the given {@code VaraibleDeclaration} without 
		 * change, otherwise visit its initializer and return a new {@code VaraibleDeclaration}
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

	
	private class VisitExpression extends AbstractChoralVisitor< Void >{

		Map<String, Integer> variableDefinedAt;
		MiniZincInput input;
		Map< Expression, Integer > dependencyExpressions;

		/** the current innermost dependency */
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
