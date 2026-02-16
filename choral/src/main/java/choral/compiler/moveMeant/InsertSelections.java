package choral.compiler.moveMeant;

import java.util.ArrayList;
import java.util.List;

import choral.ast.CompilationUnit;
import choral.ast.body.Class;
import choral.ast.body.Enum;
import choral.ast.body.ClassMethodDefinition;
import choral.ast.body.ConstructorDefinition;
import choral.ast.body.VariableDeclaration;
import choral.ast.expression.Expression;
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
import choral.utils.Pair;

public class InsertSelections {
    Selections selections;
    public InsertSelections( Selections selections ){
        this.selections = selections;
    }

    public CompilationUnit insertSelections( CompilationUnit cu ){

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
				Statement newBody = new VisitStatement().visit(constructor.blockStatements());

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
					newBody = new VisitStatement().visit(method.body().get());
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
		if( selections.enumerator() != null ) 
			newEnums.add( selections.enumerator() ); // insert enum created by KOC inference 

		return new CompilationUnit(
			old.packageDeclaration(), 
			old.imports(), 
			old.interfaces(), 
			newClasses, 
			newEnums, 
			old.position().sourceFile());
	}

    private class VisitStatement extends AbstractChoralVisitor< Statement >{
		
		
		public VisitStatement(){}

		@Override
		public Statement visit( Statement n ) {
			return n.accept( this );
		}

		@Override
		public Statement visit( ExpressionStatement n ) {
			return new ExpressionStatement(
				n.expression(), 
				visitContinutation(n.continuation()), 
				n.position());
		}

		@Override
		public Statement visit( VariableDeclarationStatement n ) {
			return new VariableDeclarationStatement(
				n.variables(), 
				visitContinutation(n.continuation()), 
				n.position());
		}

		@Override
		public Statement visit( NilStatement n ) {
			return new NilStatement(n.position());
		}

		@Override
		public Statement visit( BlockStatement n ) {
			return new BlockStatement(
				visit(n.enclosedStatement()), 
				visitContinutation(n.continuation()), 
				n.position());
		}

		@Override
		public Statement visit( IfStatement n ) {

			// Insert selections if there are any
			Statement newIfBranch = visitContinutation(n.ifBranch());
			Statement newElseBranch = visitContinutation(n.elseBranch());
			List<List<Expression>> selectionsToInsert = selections.selections().get( n );
			if( selectionsToInsert != null ){
				newIfBranch = Selections.chainSelections( newIfBranch , selectionsToInsert.get(0));
				newElseBranch = Selections.chainSelections( newElseBranch , selectionsToInsert.get(1));
			}

			return new IfStatement(
				n.condition(), 
				newIfBranch,
				newElseBranch, 
				visitContinutation(n.continuation()), n.position());
		}

		@Override // not supported
		public Statement visit( SwitchStatement n ) {
			throw new UnsupportedOperationException("SwitchStatement not supported\n\tStatement at " + n.position().toString());
		}

		@Override
		public Statement visit( TryCatchStatement n ) {
			List< Pair< VariableDeclaration, Statement > > newCatches = new ArrayList<>();
            for( Pair< VariableDeclaration, Statement > pair : n.catches() ){
                newCatches.add( 
                    new Pair<>( 
                        pair.left(), 
                        visit(pair.right()) ) );
            }
			
			return new TryCatchStatement(
				visit(n.body()), 
				newCatches, 
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

	}
}
