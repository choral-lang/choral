package choral.compiler.amend.MiniZincInference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import choral.ast.CompilationUnit;
import choral.ast.Name;
import choral.ast.Position;
import choral.ast.body.Class;
import choral.ast.body.ClassMethodDefinition;
import choral.ast.body.ConstructorDefinition;
import choral.ast.body.Enum;
import choral.ast.body.VariableDeclaration;
import choral.ast.expression.*;
import choral.ast.expression.AssignExpression.Operator;
import choral.ast.statement.*;
import choral.ast.type.TypeExpression;
import choral.ast.visitors.AbstractChoralVisitor;
import choral.compiler.amend.Utils;
import choral.compiler.amend.MiniZincInference.MiniZincInput.Dep_use;
import choral.compiler.amend.MiniZincInference.MiniZincInput.Dependency;
import choral.types.Member.HigherCallable;
import choral.utils.Continuation;

public class InsertMiniZincCommunications {

    Map<HigherCallable, MiniZincInput> inputs;
    Map<MiniZincInput, MiniZincOutput> outputs;

    Enum enum_ = null;

    public InsertMiniZincCommunications(  
        Map<HigherCallable, MiniZincInput> inputs,
        Map<MiniZincInput, MiniZincOutput> outputs
    ){
        this.inputs = inputs;
        this.outputs = outputs;
    }


    public CompilationUnit insertComs( CompilationUnit cu ){
        return createNewCompilationUnit(cu);
    }

    /**
	 * Creates a new {@code CompilationUnit} from the old, with amended method bodies (changed to 
	 * include communications) 
	 * <p>
	 * We need to create a new {@code CompilationUnit} since everything in a {@code CompilationUnit} 
	 * (in particular {@code Statements} and {@code Expressions}) are final, and can therefore not 
	 * be modified.
	 */
	private CompilationUnit createNewCompilationUnit( CompilationUnit old ){
		List<Class> newClasses = new ArrayList<>();
		for( Class cls : old.classes() ){
			List<ConstructorDefinition> newConstructors = new ArrayList<>();
			for( ConstructorDefinition constructor : cls.constructors() ){
                MiniZincInput input = inputs.get(constructor.signature().typeAnnotation().get());
                
                Statement newBody;
                if( input == null )
                    newBody = constructor.blockStatements();
                else{
					MiniZincOutput output = outputs.get(input);
                    newBody = new VisitStatement( input, output ).visitContinutation(constructor.blockStatements());
				}
					

				newConstructors.add(new ConstructorDefinition(
					constructor.signature(), 
					constructor.explicitConstructorInvocation().orElse( null ),
					newBody, 
					constructor.annotations(), 
					constructor.modifiers(), 
					constructor.position()));
			}
			
			List<ClassMethodDefinition> newMethods = new ArrayList<>();
			for( ClassMethodDefinition method : cls.methods() ){
				Statement newBody = null;
				if( method.body().isPresent() ){
                    MiniZincInput input = inputs.get(method.signature().typeAnnotation().get());
                    if( input == null )
                        newBody = method.body().get();
                    else{
						MiniZincOutput output = outputs.get(input);
						newBody = new VisitStatement( input, output ).visitContinutation(method.body().get());
					}
					    
				}

				newMethods.add(new ClassMethodDefinition(
					method.signature(), 
					newBody, 
					method.annotations(), 
					method.modifiers(), 
					method.position()));
			}

			newClasses.add(new Class(
				cls.name(), 
				cls.worldParameters(), 
				cls.typeParameters(), 
				cls.extendsClass(), 
				cls.implementsInterfaces(), 
				cls.fields(), 
				newMethods, 
				newConstructors, 
				cls.annotations(), 
				cls.modifiers(), 
				cls.position()));
		}

		List<Enum> newEnums = old.enums();
		if( enum_ != null ) 
			newEnums.add( enum_ ); // insert enum used in selections

		return new CompilationUnit(
			old.packageDeclaration(), 
			old.imports(), 
			old.interfaces(), 
			newClasses, 
			newEnums, 
			old.position().sourceFile());
	}

    
	private class VisitStatement extends AbstractChoralVisitor< Statement >{

        MiniZincInput input;
        MiniZincOutput output;
		
		public VisitStatement(MiniZincInput input, MiniZincOutput output){
            this.input = input;
            this.output = output;
        }

		@Override
		public Statement visit( Statement n ) {
			return n.accept( this );
		}

		@Override
		public Statement visit( ExpressionStatement n ) {
            Integer statementIndex = input.statementIndices.get(n).get(0);
			List<Dependency> dependencyList = output.dataCommunications.get(statementIndex);
			List<Dependency> used_at_n = getDependenciesUsedAt( statementIndex );
            
            Statement dataComs; 
			if( dependencyList == null ){
				return new ExpressionStatement(
					visitExpression(used_at_n, n.expression()), 
					visitContinutation(n.continuation()), 
					n.position());
			} else{
                dataComs = createCommunications(dependencyList, n.position());
				return Continuation.continuationAfter(
					dataComs, 
					new ExpressionStatement(
						visitExpression(used_at_n, n.expression()), 
						visitContinutation(n.continuation()), 
						n.position()));
			}
		}

		@Override
		public Statement visit( VariableDeclarationStatement n ) {
			List<Integer> statementIndices = input.statementIndices.get(n);

			List<Statement> vds = new ArrayList<>();
			for( int i = 0; i < n.variables().size(); i++ ){
				Integer statementIndex = statementIndices.get(i);
				List<Dependency> dependenciesToInsert = output.dataCommunications.get(statementIndex);
				List<Dependency> used_at_n = getDependenciesUsedAt( statementIndex );

				vds.add(visitVariableDeclaration(n.variables().get(i), dependenciesToInsert, used_at_n));
			}
			return Utils.chainStatements(vds, visitContinutation(n.continuation()));
		}

		@Override
		public Statement visit( NilStatement n ) {
			return new NilStatement(n.position());
		}

		@Override
		public Statement visit( BlockStatement n ) {
			
			return new BlockStatement(
				n.enclosedStatement(), 
				visitContinutation(n.continuation()), 
				n.position());
		}

		@Override
		public Statement visit( IfStatement n ) {

			return new IfStatement(
				n.condition(), 
				visit(n.ifBranch()),
				visit(n.elseBranch()), 
				visitContinutation(n.continuation()), n.position());
		}

		@Override // not supported
		public Statement visit( SwitchStatement n ) {
			throw new UnsupportedOperationException("SwitchStatement not supported\n\tStatement at " + n.position().toString());
		}

		@Override
		public Statement visit( TryCatchStatement n ) {
			
			return new TryCatchStatement(
				visit(n.body()), 
				n.catches(), 
				visitContinutation(n.continuation()), 
				n.position());
		}

		@Override
		public Statement visit( ReturnStatement n ) {

			return new ReturnStatement(
				n.returnExpression(), 
				visitContinutation(n.continuation()), 
				n.position());
		}

		/** 
		 * Visits the continuation if there is one 
		 */
		private Statement visitContinutation( Statement continutation ){
			return continutation == null ? null : visit(continutation);
		}

		/**
		 * Creates a communication of all the dependencies given. These statements are chained 
		 * together, and the first statement is returned. 
		 */
        private Statement createCommunications( List<Dependency> dependencies, Position pos ){
            List< VariableDeclaration > variables = new ArrayList<>();
            for( Dependency dependency : dependencies ){
                createVariables(dependency, variables, pos);
            }

            if( variables.size() > 0 )
                return chainVariables(variables, new NilStatement(pos));
            else
                return null; 
        }

		/**
		 * Creates a variable for the given dependency and all of its nested dependencies.
		 */
		private void createVariables( 
			Dependency dependency, 
			List< VariableDeclaration > variables,
			Position position
		){		
			// find nested dependencies
			List< Dependency > nestedDependencies = getNestedDependencies(dependency);
			
			// the output has already generated the variable names for the dependencies
			Name variableName = output.dependencyVariables.get(dependency);
			
			// visit the dependency to solve nested dependencies
			Expression visitedDependencyExpression = new VisitExpression(output.dependencyVariables, nestedDependencies).visit(dependency.originalExpression());
			
			// create the AssignExpression
			AssignExpression initializer = new AssignExpression(
				dependency.createComExpression( visitedDependencyExpression ), 
				new FieldAccessExpression(variableName), 
				Operator.ASSIGN);
			
			// create the VariableDeclaration and add it to the list of variable declarations
			variables.add( new VariableDeclaration(
				variableName, 
				dependency.getType(), 
				Collections.emptyList(), 
				initializer, 
				position));
			
			System.out.println( "Dependency: Role " + dependency.recipient() + " needs " + dependency.originalExpression() );
			System.out.println( "Saved in variable: " + variableName );
		}

        /**
         * Retreives the nested dependencies of a dependency
         */
        private List<Dependency> getNestedDependencies( Dependency dependency ){
            List<Dependency> nestedDeps = new ArrayList<>();
            Integer dep_idx = input.dependencies.indexOf(dependency);
            for( MiniZincInput.Dep_use dep_use : input.dep_used_at ){
                if( dep_use.used_at-1 == dep_idx && dep_use.nested_dependency ){
                    nestedDeps.add(input.dependencies.get(dep_use.dependency-1));
                }
            }
            return nestedDeps;
        }

		/**
		 * Retruns all the dependencies that are used at the given statement index
		 */
		private List<Dependency> getDependenciesUsedAt( Integer statementIndex ){
			List<Dependency> used_at = new ArrayList<>();
			for( Dep_use dep_use : input.dep_used_at ){
				if( dep_use.used_at == statementIndex && !dep_use.nested_dependency )
					used_at.add(input.dependencies.get( dep_use.dependency-1 ));
			}
			return used_at;

		}

		/**
		 * Chain together variable declarations.
		 * <p>
		 * Puts the continuation as the continuation of the last VariableDeclaration in remainingVariables.
		 */
		private Statement chainVariables( List< VariableDeclaration > remainingVariables, Statement continuation ){
			if( remainingVariables.size() == 0 ){
                return continuation;
            }
            VariableDeclaration variable = remainingVariables.remove(remainingVariables.size()-1);
            VariableDeclarationStatement variableStaement = new VariableDeclarationStatement(
                List.of( variable ), 
                continuation,
                continuation.position());
            return chainVariables(remainingVariables, variableStaement);
		}

		/**
		 * For some initial {@code Expression} and a list of {@code Dependency}s, visits the 
		 * initial {@code Expression} with the dependency list.
		 */
		private Expression visitExpression( List<Dependency> dependencyList, Expression first ){
			return new VisitExpression( output.dependencyVariables, dependencyList ).visit(first);
		}

		/**
		 * If there is no initializer, return the given {@code VaraibleDeclaration} without 
		 * change, otherwise visit its initializer and return a new {@code VaraibleDeclaration}
		 */
		private Statement visitVariableDeclaration( 
			VariableDeclaration vd, 
			List<Dependency> dependenciesToInsert,
			List<Dependency> used_at_n
		){
			Statement coms = createCommunications(dependenciesToInsert, vd.position());
			VariableDeclaration newVd;
			if( vd.initializer().isEmpty() )
				newVd = vd;
			else
				newVd = new VariableDeclaration(
					vd.name(), 
					vd.type(), 
					vd.annotations(), 
					(AssignExpression)visitExpression(used_at_n, vd.initializer().get()),
					vd.position());
			
			return Continuation.continuationAfter(
				coms, 
				new VariableDeclarationStatement(
					List.of(newVd), 
					new NilStatement( vd.position() ), 
					vd.position()));
		}

	}

    /**
	 * Replaces nested dependencies with the variables in which they are saved.
	 * <p>
	 * Iterates through {@code Expression}s and checks if they are equal to any of the nested 
     * dependencies. If an {@code Expression} is equal to a dependency it is replaced with the
     * variable in which that dependency is saved.
	 */
	private class VisitExpression extends AbstractChoralVisitor< Expression >{

        Map<Dependency, Name> dependencyVariables;
        List<Dependency> nestedDependencies;

		public VisitExpression(
            Map<Dependency, Name> dependencyVariables,
            List<Dependency> nestedDependencies
        ){
            this.dependencyVariables = dependencyVariables;
            this.nestedDependencies = nestedDependencies;
        }

		@Override
		public Expression visit( Expression n ) {
			return n.accept( this );
		}

		@Override
		public Expression visit( ScopedExpression n ) {
			Expression dependencyCheck = checkIfDependency(n);
			if( dependencyCheck != null ) {
				return dependencyCheck;
			}
			
			Expression newscopedExpression = visit(n.scopedExpression());

			return new ScopedExpression(
				n.scope(), // Dependencies cannot be in the scope, so no need to check
				newscopedExpression, 
				n.position());
		}

		@Override
		public Expression visit( FieldAccessExpression n ) {
			Expression dependencyCheck = checkIfDependency(n);
			if( dependencyCheck != null ) {
				return dependencyCheck;
			}
			return n;
		}

		@Override
		public Expression visit( MethodCallExpression n ) {
			Expression dependencyCheck = checkIfDependency(n);
			if( dependencyCheck != null ) {
				return dependencyCheck;
			}

			List<Expression> newArgs = new ArrayList<>();
			for(int i = 0; i < n.arguments().size(); i++){
				Expression argument = n.arguments().get(i);
				
				// visit all of the arguments and add them to the new list of arguments
				newArgs.add( visit(argument) );
			}

			return new MethodCallExpression(
				n.name(), 
				newArgs, 
				n.typeArguments(), 
				n.position());
		}
		
		@Override
		public Expression visit( AssignExpression n ) {
			// an assignExpression cannot itself be a dependency, but its value() might

			Expression newValue = visit( n.value() );

			return new AssignExpression(
				newValue, 
				n.target(), 
				n.operator(),
				n.position());
		}

		@Override
		public Expression visit( BinaryExpression n ) {
			Expression dependencyCheck = checkIfDependency(n);
			if( dependencyCheck != null ) {
				return dependencyCheck;
			}
			
			Expression newLeft = visit( n.left() );
			Expression newRight = visit( n.right() );
			
			return new BinaryExpression(
				newLeft, 
				newRight, 
				n.operator(), 
				n.position());
		}

		@Override
		public Expression visit( EnclosedExpression n ) {
			Expression dependencyCheck = checkIfDependency(n);
			if( dependencyCheck != null ) {
				return dependencyCheck;
			}

			Expression newNestedExpression = visit( n.nestedExpression() );
			
			return new EnclosedExpression(
				newNestedExpression, 
				n.position());
		}
		
		@Override
		public Expression visit( StaticAccessExpression n ) {
			Expression dependencyCheck = checkIfDependency(n);
			if( dependencyCheck != null ) {
				return dependencyCheck;
			}
			return n;
		}

		@Override
		public Expression visit( ClassInstantiationExpression n ) {
			Expression dependencyCheck = checkIfDependency(n);
			if( dependencyCheck != null ) {
				return dependencyCheck;
			}

			List<Expression> newArgs = new ArrayList<>();
			for(int i = 0; i < n.arguments().size(); i++){
				Expression argument = n.arguments().get(i);
				
				// visit all of the arguments and add them to the new list of arguments
				newArgs.add( visit(argument) );
			}

			return new ClassInstantiationExpression(
				n.typeExpression(), 
				newArgs, 
				n.typeArguments(), 
				n.position());
		}

		@Override
		public Expression visit( NotExpression n ) {
			Expression dependencyCheck = checkIfDependency(n);
			if( dependencyCheck != null ) {
				return dependencyCheck;
			}

			Expression newExpression = visit( n.expression() );
			
			return new NotExpression(
				newExpression, 
				n.position());
		}

		@Override // not supported
		public Expression visit( ThisExpression n ) {
			throw new UnsupportedOperationException("ThisExpression not supported\n\tExpression at " + n.position().toString());
		}

		@Override // not supported
		public Expression visit( SuperExpression n ) {
			throw new UnsupportedOperationException("SuperExpression not supported\n\tExpression at " + n.position().toString());
		}

		@Override
		public Expression visit( NullExpression n ) {
			// Nothing can depend on null, so no need to check dependencies
			return n;
		}

		public Expression visit( LiteralExpression.BooleanLiteralExpression n ) {
			// literals are not permited to be in dependencies
			return n;
		}

		public Expression visit( LiteralExpression.IntegerLiteralExpression n ) {
			// literals are not permited to be in dependencies
			return n;
		}

		public Expression visit( LiteralExpression.DoubleLiteralExpression n ) {
			// literals are not permited to be in dependencies
			return n;
		}

		public Expression visit( LiteralExpression.StringLiteralExpression n ) {
			// literals are not permited to be in dependencies
			return n;
		}

		@Override // not supported
		public Expression visit( TypeExpression n ) {
			throw new UnsupportedOperationException("TypeExpression not supported\n\tExpression at " + n.position().toString());
		}

		@Override // not supported
		public Expression visit( BlankExpression n ){
			throw new UnsupportedOperationException("BlankExpression not supported\n\tExpression at " + n.position().toString());
		}

		@Override // not supported
		public Expression visit( EnumCaseInstantiationExpression n ){
			throw new UnsupportedOperationException("EnumCaseInstantiationExpression not supported\n\tExpression at " + n.position().toString());
		}

		/**
		 * Checks if an {@code Expression} is a {@code Dependency}. If so, returns a 
         * {@code FieldAccessExpression} accessing the variable in which the dependency is saved. 
         * <p>
         * If the {@code Expression} is not a {@code Dependency}, return null.
		 */
		private Expression checkIfDependency( Expression n ){
			for( Dependency dependency : nestedDependencies ){
				if( Utils.isSameExpression(n, dependency.originalExpression()) ){
					return new FieldAccessExpression( dependencyVariables.get(dependency) );
				}
			}
			return null;
		}
	}
}
