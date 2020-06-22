package choral.examples.HelloRoles;
import org.choral.annotations.Choreography;

@Choreography( role = "B", name = "HelloRoles" )
class HelloRoles_B {
	public void sayHello() {
		String b;
		b = "Hello from B";
		System.out.println( b );
	}

}
