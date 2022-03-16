//import my.fancy.choral.library;

 interface Channel@( S, R ) {
     < T@( R ) extends Serializable@( R ), T@( S ) extends Serializable@( R ) > T@( R ) com( T@( S ) msg );
 }

 interface Logger@( L ) {
     void@( L ) write( String@( L ) msg );
 }

 class B@( P ) {
 	Boolean@( P ) b1 b2;
 	Integer@( P ) i;
 	Float@( P ) f;
 	String@( P ) s;

 	static void@( P ) main() {
 		b = true;
 		i = 42;
 		f = 3.14;
 		s = "Ciao";
 	}
 }

 class C@( W1, W2 ) <
         T1@( W1, W2 ),
         T2@( W3 ) extends T1@( W3 )
			 > implements B@( W1, W2 )< T1@( W1 ), T2@( W2 ) > {}

 class LoggingChannel@( S, R, L ) implements Channel@( S, R ) {

   Logger@( L ) log;
   Channel@( S, L ) logChannel;
   Channel@( S, R ) comChannel;

    LoggingChannel( Logger@( L ) log, Channel@( S, L ) logChannel, Channel@( S, R ) comChannel ) {
         this.log = log;
         this.logChannel = logChannel;
         this.comChannel = comChannel;
    }

    < T@( S ) extends Serializable@( S ), T@( R ) extends Serializable@( R ) > T@( R ) com( T@( S ) msg ){
        log.write( logChannel.com( msg ) );
        return comChannel.com( msg );
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
