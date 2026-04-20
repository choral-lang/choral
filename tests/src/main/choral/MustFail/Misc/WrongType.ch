package choral.examples.HelloRoles;

class WrongType@( A ) {
    public void sayHello() {
        String@A a = "Hello from A"@A;
        int@A lol = 5@A;
        int@A lul = "Hello"@A; //! Required type 'int@(A)', found 'java.lang.String@(A)' 
        a = lol; 
        int@A lmao = 5.5@A; 
        System@A.out.println( a );
    }
}
