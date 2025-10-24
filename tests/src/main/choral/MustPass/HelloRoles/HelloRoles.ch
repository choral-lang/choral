package choral.MustPass.HelloRoles;

class HelloRoles@( A, B ) {
	public void sayHello() {
		String@A a = "Hello from A"@A;
    	String@B b = "Hello from B"@B;
		System@A.out.println( a );
		System@B.out.println( b );
	}
}
