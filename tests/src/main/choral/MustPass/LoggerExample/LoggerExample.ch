interface Logger@( L ) {
    void write( String@( L ) msg );
}

class B@( P ) {
	static Boolean@( P ) b1, b2;
	static Integer@( P ) i;
	static Double@( P ) f;
	static String@( P ) s;

	static void main() {
		b1 = true@P;
		i = 42@P;
		f = 3.14@P;
		s = "Ciao"@P;
	}
}

class Random@( A ) {

	Integer@( A ) seed;

	Random( Integer@( A ) seed ){
		this.seed = seed;
	}

	static Double@( A ) getRandom(){
		return Math@( A ).random();
	}

}
