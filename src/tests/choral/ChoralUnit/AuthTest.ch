package choral.testSuite.ChoralUnit;

public class AuthTest@( A, B ) {

	@Test
	public static void test1(){
		SymChannel@( A, B )< Object > c = TestUtils@( A, B ).newLocalChannel( "AuthTestChannel1"@[ A, B ] );
		EnumBoolean@A result = new Auth@( A, B )( c ).run( "USR"@A, "PSW"@A );
		Assert@A.assertEquals( result, EnumBoolean@A.True, "Test 1: Valid login tested correctly"@A, "Test 1: error"@A );
	}

	@Test
	public static void test2(){
		Auth@( A, B ) auth =
			TestUtils@( A, B ).newLocalChannel( "AuthTestChannel2"@[ A, B ] )
			>> Auth@( A, B )::new;
		EnumBoolean@A result = auth.run( "WRONG_USER"@A, "PSW"@A );
		Assert@A.assertEquals( result, EnumBoolean@A.False, "Test 2: Invalid login tested correctly"@A, "Test 2: error"@A );
	}

	@Test
	public static void test3(){
		Auth@( A, B ) auth =
			TestUtils@( A, B ).newLocalChannel( "AuthTestChannel3"@[ A, B ] )
			>> Auth@( A, B )::new;
		EnumBoolean@A result = auth.run( "WRONG_USER"@A, "PSW"@A );
		try {
			Assert@A.assertEquals( result, EnumBoolean@A.True, "Test 3: error, should have failed"@A, "Test 3: should fail"@A );
		} catch( AssertException@A e ){
			System@A.out.println( "Assertion Exception correctly caught in Test 3"@A );
		}
	}

	@Test
	public static void test4(){
	Auth@( A, B ) auth =
    			TestUtils@( A, B ).newLocalChannel( "AuthTestChannel4"@[ A, B ] )
    			>> Auth@( A, B )::new;
    		Boolean@B result = auth.checkID( 1@B );
			Assert@B.assertEquals( result, Boolean@B.TRUE, "Test 4: equality check passed"@B, "Test 4: error"@B );
	}

}
