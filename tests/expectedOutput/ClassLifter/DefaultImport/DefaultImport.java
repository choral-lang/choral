package ClassLifter.DefaultImport;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "DefaultImport" )
class DefaultImport {
	public void test() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
	}

}
