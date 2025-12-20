import org.junit.jupiter.api.Test;
import java.util.List;
import org.eclipse.lsp4j.Diagnostic;
import lsp.features.*;

public class LSPTest {
	DiagnosticsProvider diagProvider = new DiagnosticsProvider();

	@Test
	public void sayHello() {
		String code = """
				package choral.MustPass.HelloRoles;
				class HelloRoles@(A, B){
				    public void sayHello(){
				        String@A a = "Hello from A"@A;
				        String@B b = "Hello from B"@B;
				        System@A.out.println(a);
				        System@B.out.println(b);
				    }
				}""";

		List< Diagnostic > diagnostics = diagProvider.analyze( "", code );
		assert diagnostics.isEmpty();
	}

	@Test
	public void wrongType() {
		String code = """
                package choral.examples.HelloRoles;
                class WrongType@( A ) {
                    public void sayHello() {
                        String@A a = "Hello from A"@A;
                        int@A lol = 5@A;
                        int@A lul = "Hello"@A;
                        a = "lol"@A;
                    }
                }
                """; // int@A = 5.5@A; // a = "lol";

		List<Diagnostic> diagnostics = diagProvider.analyze("", code);
		assert diagnostics.size() == 1;
		assert diagnostics.get(0).getMessage().contains("StaticVerificationException");
	}
}
