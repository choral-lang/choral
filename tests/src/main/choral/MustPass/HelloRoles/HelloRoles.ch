package choral.MustPass.HelloRoles;

class HelloRoles@( A, B ) {
	public void sayHello() {
		String@A a = "Helo from A"@A;
    	String@B b = "Helo from B"@B;
		System@A.out.println( a );
		System@B.out.println( b );
	}
}
