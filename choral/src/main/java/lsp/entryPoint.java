package lsp;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;

import lsp.features.*;

public class entryPoint {
    public static void main(String[] args) {
        DiagnosticsProvider diagProvider = new DiagnosticsProvider();
        String okayCode = """
            package choral.MustPass.HelloRoles;

            class HelloRoles@(A, B){
                public void sayHello(){
                    String@A a = "Hello from A"@A;
                    String@B b = "Hello from B"@B;
                    System@A.out.println(a);
                    System@B.out.println(b);
                }
            }""";

        String notOkayCode = """
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
        List<Diagnostic> diagnostics = diagProvider.analyze("", notOkayCode);
        for (Diagnostic diag : diagnostics){
            System.out.println("Diagnostic message: " + diag.getMessage());
            System.out.println("Diagnostic position: " + diag.getRange());
        }
        
        System.out.println("Errors: " + diagnostics.size());

    }
}
