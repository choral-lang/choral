package ClassLifter.DefaultImport;

class DefaultImport@( A ) {
	public void test(){
        ClassLoader@A cl = ClassLoader@A.getSystemClassLoader();
	}
}
