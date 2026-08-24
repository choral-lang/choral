package Typer.StaticContextNonStaticMethod;

class StaticContextNonStaticMethod@( A ) {
	public static void fun() {
		helper(); //! Cannot resolve method
	}

	private void helper() {
	}
}
