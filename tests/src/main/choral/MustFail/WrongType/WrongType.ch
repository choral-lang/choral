package choral.examples.HelloRoles;

class WrongType@( A ) {
    public void sayHello() {
        String@A a = "Hello from A"@A;
        int@A lol = 5@A;
        int@A lul = "Hello"@A; //! Required type 'int@(A)', found 'java.lang.String@(A)' //! Required type 'int@(A)', found 'double@(A)'
        a = lol; //! Required type 'java.lang.String@(A)', found 'int@(A)'
        int@A lmao = 5.5@A; //! Required type 'int@(A)', found 'double@(A)' 
        System@A.out.println( a );
    }
}
