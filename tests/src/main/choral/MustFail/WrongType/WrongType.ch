package choral.examples.HelloRoles;

class WrongType@( A ) {
    public void sayHello() {
        String@A a = "Hello from A"@A;
        int@A lol = 5@A;
        int@A lul = "Hello"@A; // expectedError: Required type 'int@(A)', found 'java.lang.String@(A)'; // expectedError: Required type 'int@(A)', found 'double@(A)';
        a = lol; // expectedError: Required type 'java.lang.String@(A)', found 'int@(A)';
        int@A lmao = 5.5@A; // expectedError: Required type 'int@(A)', found 'double@(A)' ;
        System@A.out.println( a );
    }
}
