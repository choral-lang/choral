package choral.MustPass.MoveMeant.AmbiguousRecipient1.utils;

import java.lang.Object;
import java.lang.Integer;

public class Client@(A,B) {

	Object@A obj;

	public void fun( Integer@A in ){}

    public void fun( Integer@B in ){}
    
    public void fun( Integer@A in1, Integer@A in2 ){}
    
    public void fun( Integer@A in1, Integer@B in2 ){}

}
