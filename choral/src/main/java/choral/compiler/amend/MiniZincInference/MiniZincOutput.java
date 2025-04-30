package choral.compiler.amend.MiniZincInference;

import java.util.List;
import java.util.Map;

import choral.ast.Name;
import choral.compiler.amend.Utils;
import choral.compiler.amend.MiniZincInference.MiniZincInput.Dependency;
import choral.exceptions.CommunicationInferenceException;
import choral.types.GroundInterface;
import choral.types.World;
import choral.utils.Pair;

public class MiniZincOutput{
    Map<Integer, Dependency> dataCommunications;
    Map<Dependency, Name> dependencyVariables;
    Map<Integer, MiniZincSelectionMethod> selections;

    public void insertDataCom( Integer idx, Dependency dep ){
        dataCommunications.put(idx, dep);
        dependencyVariables.put(dep, new Name( "dependencyAt" + dep.recipient() + "_" + Math.abs(dep.originalExpression().hashCode()) ));
    }

    public void insertSelection( 
        World recipient, 
        World sender, 
        List<Pair<String, GroundInterface>> chanels,
        Integer idx
    ){
        MiniZincSelectionMethod selectionMethod = 
            (MiniZincSelectionMethod)Utils.findSelectionMethod(recipient, sender, chanels);
        if( selectionMethod == null )
            throw new CommunicationInferenceException("No viable selection method was found for " + recipient + " with sender " + sender);
        selections.put(idx, selectionMethod);
    }

}
