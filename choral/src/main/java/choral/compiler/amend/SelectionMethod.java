package choral.compiler.amend;

import choral.types.GroundInterface;
import choral.types.Member.HigherMethod;
import choral.types.World;

public class SelectionMethod {
    protected String channelIdentifier;
    protected GroundInterface channel;
    protected HigherMethod selectionMethod;
    protected World sender;

    public SelectionMethod( 
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
}
