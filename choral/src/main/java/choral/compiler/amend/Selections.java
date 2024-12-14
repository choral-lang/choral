package choral.compiler.amend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import choral.ast.body.Enum;
import choral.ast.expression.Expression;
import choral.ast.statement.ExpressionStatement;
import choral.ast.statement.Statement;

public class Selections {

    private Map< Statement, List<List<Expression>> > selections = new HashMap<>();
    private Enum enumerator;
    
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
