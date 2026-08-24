package Typer.ConcreteImplementsAbstract;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Base" )
abstract class Base {
	public abstract void foo();
	
	public abstract String bar();

}
