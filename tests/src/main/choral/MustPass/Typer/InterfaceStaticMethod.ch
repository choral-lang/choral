package choral.Typer.InterfaceStaticMethod;

public interface InterfaceStaticMethod@( A ) {
    static String@A greet( String@A name ) {
        return "Hello, "@A + name;
    }
}

class UseGreeter@( A ) {
    public String@A go( String@A name ) {
        return InterfaceStaticMethod@A.greet( name );
    }
}