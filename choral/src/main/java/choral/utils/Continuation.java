package choral.utils;

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

public class Continuation {

	/**
	 * Puts continuation as the continuation of the statement at the end of statement's 
	 * "continuation-chain".
	 * <p>
	 * The "continuation-chain" is what you get when continuously following the continuation 
	 * field of a statement. Continuation-chains will end in a null continuation or a 
	 * NilStatement continuation.
	 */
    public static Statement continuationAfter( Statement statement, Statement continuation ){
        if( statement == null || statement instanceof NilStatement )
            return continuation;

        return new VisitStatement( continuationAfter( statement.continuation(), continuation ) ).visit( statement );
    }


    private static class VisitStatement extends AbstractChoralVisitor< Statement >{
		
        Statement continuation;
		
		public VisitStatement( Statement continuation ){
            this.continuation = continuation;
        }

		@Override
		public Statement visit( Statement n ) {
			return n.accept( this );
		}

		@Override
		public Statement visit( ExpressionStatement n ) {
            return new ExpressionStatement(
				n.expression(), 
				continuation, 
				n.position());
		}

		@Override
		public Statement visit( VariableDeclarationStatement n ) {
            return new VariableDeclarationStatement(
				n.variables(), 
				continuation, 
				n.position());
		}

		@Override
		public Statement visit( NilStatement n ) {
			return new NilStatement(n.position());
		}

		@Override
		public Statement visit( BlockStatement n ) {
			return new BlockStatement(
				n.enclosedStatement(), 
				continuation, 
				n.position());
		}

		@Override
		public Statement visit( IfStatement n ) {
			return new IfStatement(
				n.condition(), 
				n.ifBranch(),
				n.elseBranch(), 
				continuation, 
                n.position());
		}

		@Override 
		public Statement visit( SwitchStatement n ) {
			return new SwitchStatement(
                n.guard(), 
                n.cases(), 
                continuation, 
                n.position());
		}

		@Override
		public Statement visit( TryCatchStatement n ) {
			return new TryCatchStatement(
				n.body(), 
				n.catches(), 
				continuation, 
				n.position());
		}

		@Override
		public Statement visit( ReturnStatement n ) {
			return new ReturnStatement(
				n.returnExpression(), 
				continuation, 
				n.position());
		}
	}
    
}
