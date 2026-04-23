package Typer.ConcreteInheritsConcreteFromSuperclass;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "ConcreteInheritsConcreteFromSuperclass" )
class ConcreteInheritsConcreteFromSuperclass extends Base {
	public String use() {
		return greet();
	}

}
