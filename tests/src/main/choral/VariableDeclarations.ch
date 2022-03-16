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

		// Multiple variable declarations + assignments
		Integer@A g, h = 13@A, i = 2@A, j, k, l = 1337@A;

		// Declaration with assignment at wrong role - SHOULD FAIL
		// String@A m = "test"@B;

		// Declaration with assignment of wrong type - SHOULD FAIL
		// Integer@A n, o = 3@A, p = "fail"@A, r;
	}
}
