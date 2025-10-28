package choral.MustPass.HelloRoles;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "HelloRoles" )
class HelloRoles_A {
	public void sayHello() {
		String a = "Helo from A";
		System.out.println( a );
	}

}
