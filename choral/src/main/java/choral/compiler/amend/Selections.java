package choral.compiler.amend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import choral.ast.body.Enum;
import choral.ast.expression.Expression;
import choral.ast.statement.ExpressionStatement;
import choral.ast.statement.Statement;

/**
 * An object to contain all the selections of a CompilationUnit. 
 * <p>
 * Contains a map from Statement to List of List of Expression. These Statements are either IfStatements
 * or SwitchStatements, and are mapped to all the selections of that Statement. The outermost List 
 * contains an element (A List of Expressions) for each branch of the Statement, and the inner List 
 * contain one element (an Expression) for each selection in that branch (note that all these inner 
 * lists shoule be the same length) and each Expression is a selection.
 */
public class Selections {

    private Map< Statement, List< List<Expression>> > selections = new HashMap<>();
    private Enum enumerator;
    
    public Selections(){}

    public Selections(
        Map< Statement, List<List<Expression>> > selections,
        Enum enumerator
    ){
        this.selections = selections;
        this.enumerator = enumerator;
    }

    public Map< Statement, List<List<Expression>> > selections(){
        return selections;
    }

    public Enum enumerator(){
        return enumerator;
    }

    /**
     * Takes a list of selection-expressions and a Statement, and turns all the expressions into
     * Statements and puts them in front of the input statement.
     * <p>
     * This is done by removing the last element of the list of selections and creating a 
     * Statement with that as its body, and the input Statement as its continutation. The method
     * then calls itself recursively on the newly created Statement and the list of remainig 
     * selections, untill the list of selections becomes empty.
     * <p>
     * Note that this keeps the order of the list of expressions. 
     */
    public static Statement chainSelections( Statement statement, List<Expression> remainingSelections ){
            if( remainingSelections.size() == 0 ){
                return statement;
            }
            Expression selection = remainingSelections.remove(remainingSelections.size()-1);
            ExpressionStatement selectionStatement = new ExpressionStatement(
                selection, 
                statement,
                statement.position());
            return chainSelections(selectionStatement, remainingSelections);
        }

}
