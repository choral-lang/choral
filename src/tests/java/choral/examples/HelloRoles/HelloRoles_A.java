package choral.examples.HelloRoles;
import org.choral.annotations.Choreography;

@Choreography( role = "A", name = "HelloRoles" )
class HelloRoles_A {
	public void sayHello() {
		String a;
		a = "Hello from A";
		System.out.println( a );
	}

}
