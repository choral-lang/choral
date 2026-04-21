package choral.MustPass.Typer.StandardLibraryReduction;

import java.math.BigInteger;
import java.util.Optional;

class StandardLibraryReduction@( A, B ) {
	public void test(){
        int@A a = 3@A;
        int@A b = 7@A;
        Math@A.max(a, b);
        BigInteger@A x = new BigInteger@A("1999"@A);
        Optional@A<String> none = Optional@A.<String>of("S"@A);
	}
}
