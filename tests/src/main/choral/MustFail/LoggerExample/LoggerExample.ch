interface Logger@( L ) {
    void write( String@( L ) msg );
}

class B@( P ) {
	Boolean@( P ) b1, b2;
	Integer@( P ) i;
	Float@( P ) f;
	String@( P ) s;

	static void main() {
		b = true@P;
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

	static Integer@( A ) getRandom(){
		return Math@( A ).getRandom();
	}

}
