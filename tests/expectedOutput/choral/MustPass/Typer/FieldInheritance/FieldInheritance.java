package choral.MustPass.Typer.FieldInheritance;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "FieldInheritance" )
class FieldInheritance extends Base {
	public void test() {
		String x = this.name;
	}

}
