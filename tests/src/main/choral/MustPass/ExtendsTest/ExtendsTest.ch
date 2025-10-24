package choral.MustPass.ExtendsTest;

interface MyInterface@( A, B ){}

interface MyExtInterface@( A, B ) extends MyInterface@( A, B ){}

interface MyOtherInterface@( A, B ){}

class MyClass@( A, B ) implements MyExtInterface@(A, B){}

class MyExtClass@( A, B ) extends MyClass@( A, B ) implements MyOtherInterface@( A, B ){}
