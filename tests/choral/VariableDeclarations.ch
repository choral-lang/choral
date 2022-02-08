class VariableDeclarations@(A, B) {
	void method() {
		// Single variable declaration
		String@A a;

		// Single variable declaration + assignment
		String@A b = "test"@A;

		// Multiple variable declarations
		Integer@A c, d, e, f;

		// Multiple variable assignments
		c = d = e = f = 42@A;

		// Multiple variable declarations + assignments (SHOULD FAIL - SYNTAX NOT SUPPORTED!)
		// Bool@A g, h, i = true@A;

		// Declaration with assignment at wrong role - SHOULD FAIL
		// String@A j = "test"@B;
	}
}