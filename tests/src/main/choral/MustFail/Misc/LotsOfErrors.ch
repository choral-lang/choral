/** TEST ***************************************************************************************************************
* Components: type checking, type declaration extractor
* Criteria: passed if rejected by the compiler
* Description: contains ill-formed type declarations to stress the checks performed by the type declaration extractor:
*    - duplicated declarations of data types, world parameters, type parameters, fields, enum cases
*    - undefined and duplicated world arguments in type applications
***********************************************************************************************************************/

class LotsOfErrors@W<T@X extends A@W>{
    LotsOfErrors@W f;
    T@W f;     // duplicate field
    LotsOfErrors@Y g;     // undefined world argument
    LotsOfErrors@(W,W) h; // world arguments are not distinct and incompatible kinds in type application
    LotsOfErrors i;
}

class B@W extends LotsOfErrors@W2 /* undefined world argument*/ { B(){} }

enum E@W {
    // no cases
}

class D@(W,W /* duplicate world parameter */ ){} //! Duplicate role parameter 'W'

class C@(W1,W2) { C(){} } // this is ok

class C@W1 { C(){} } // duplicated definition
