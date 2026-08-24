package choral.compiler.moveMeant.MiniZincInference;

import choral.types.World;

import java.util.Collections;
import java.util.List;

import choral.ast.Name;
import choral.ast.Position;
import choral.ast.body.Enum;
import choral.ast.body.EnumConstant;
import choral.ast.expression.Expression;
import choral.ast.expression.FieldAccessExpression;
import choral.ast.expression.MethodCallExpression;
import choral.ast.expression.ScopedExpression;
import choral.ast.expression.StaticAccessExpression;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.types.GroundInterface;
import choral.types.Member.HigherMethod;

/**
 * A class to store everything needed to create a specific selection except the Enum.
 */
public class MiniZincSelectionMethod{

    protected String channelIdentifier;
    protected GroundInterface channel;
    protected HigherMethod selectionMethod;
    protected World sender;

    public MiniZincSelectionMethod( 
        String channelIdentifier,
        GroundInterface channel,
        HigherMethod selectionMethod,
        World sender 
    ){
        this.channelIdentifier = channelIdentifier;
        this.channel = channel;
        this.selectionMethod = selectionMethod;
        this.sender = sender;
    }

    public String channelIdentifier(){
        return channelIdentifier;
    }

    public GroundInterface channel(){
        return channel;
    }

    public HigherMethod selectionMethod(){
        return selectionMethod;
    }

    public World sender(){
        return sender;
    }

    /**
     * Creates a selections expression from this SelectionMethod object on the enumerator given as 
     * input.
     */
    public ScopedExpression createSelectionExpression( Enum enumerator, EnumConstant enumCons, Position position ){
        
        TypeExpression typeExpression = new TypeExpression( 
            enumerator.name(), 
            Collections.emptyList(), // This needs to be "higher kinded" and can thus not have a worldargument
            Collections.emptyList(),
            position);

        TypeExpression argScope = new TypeExpression(
            enumerator.name(), 
            List.of( new WorldArgument( new Name(sender.identifier() )) ), // this needs a worldargument
            Collections.emptyList(),
            position);

        ScopedExpression argument = new ScopedExpression( // looks something like Enum@Sender.CHOICE
            new StaticAccessExpression( // Enum@Sender
                argScope,
                position),
            new FieldAccessExpression( // CHOICE
                enumCons.name(),
                position),
            position);
        
        final List<Expression> arguments = List.of( argument );
        final Name name = new Name( selectionMethod.identifier() );
        final List<TypeExpression> typeArguments = List.of( typeExpression );
        
        MethodCallExpression scopedExpression = new MethodCallExpression(name, arguments, typeArguments, position);
        
        // The selection method is a method inside its channel, so we need to make the channel its scope
        FieldAccessExpression scope = new FieldAccessExpression(new Name(channelIdentifier), position);
        
        // Something like channel.< enumerator >select( enumerator@sender.enumCons )
        return new ScopedExpression(scope, scopedExpression, position);
    }
}
