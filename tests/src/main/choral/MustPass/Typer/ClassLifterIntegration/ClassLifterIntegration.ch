package choral.MustPass.Typer.ClassLifterIntegration;

import java.util.Random;

class ClassLifterIntegration@( A, B ) {
	public void test(){
		String@A test = "test"@A;
		Random@A rn = new Random@A();
		rn.nextInt();
	}
}
