package choral.compiler.amend;

import choral.ast.CompilationUnit;

public class InferCommunications {
    
    public static CompilationUnit inferCommunications( CompilationUnit cu ){

        Selections selections = new BasicKOCInference().inferKOC( cu );

        /* 
         * I want to keep the two types of inference (knowledge of choice and data 
         * inference) seperated, but they want accesss to the original 
         * CompilationUnit. KOC wants to be able to see which roles are part of a 
         * statement, and data wants to compare the original expressions to a list 
         * of dependencies. 
         * Because of this, I cannot have the KOC inference return a changed 
         * CompilationUnit, so I instead have it return a Selections Object, and 
         * leave it up to the data inference to insert these selections.
         */

        return new BasicDataInference( selections ).inferComms(cu);
    }
}
