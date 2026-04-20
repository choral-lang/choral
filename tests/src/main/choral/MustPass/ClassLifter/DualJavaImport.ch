package choral.MustPass.ClassLifter.DualJavaImport;

import java.util.ArrayDeque;
import java.util.Deque;

class DualJavaImport@( A, B ) {
	public void test(){
		String@A test = "test"@A;
        Deque@A<String> d = new ArrayDeque@A<String>();
        d.addFirst("a"@A);
        d.addLast("b"@A);
        
	}
}
