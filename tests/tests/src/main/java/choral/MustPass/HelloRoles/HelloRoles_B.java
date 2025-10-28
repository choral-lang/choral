package choral.MustPass.HelloRoles;

import choral.annotations.Choreography;

@Choreography( role = "B", name = "HelloRoles" )
class HelloRoles_B {
	public void sayHello() {
		String b = "Helo from B";
		System.out.println( b );
	}

}
