package Misc.MultiFileError;

import Misc.MultiFileError.ErrorHelper;

class MultiFileError@(A,B){
    public void Run(){
        String@A msg = "Hello"@A;
        msg = ErrorHelper@A.Helper(); //! Required type 'java.lang.String@(A)', found 'int@(A)'
    }
}