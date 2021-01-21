public class AutoBoxing@( A, B ) {
        public void f(){
            int@A numberPrimitive;
            Integer@A numberObject;

            // no (un)boxing necessary
            numberPrimitive = f1( 2@A );
            numberObject    = f1( Integer@A.valueOf( 2@A ) );

            // (un)boxing in assignments
            numberPrimitive  = Integer@A.valueOf( 2@A );
            numberObject = 2@A;

            // unboxing in invocations
            f2( Integer@A.valueOf( 2@A ) );

            // boxing in role-overloaded invocation
            f3( 2@B );
            f4( Integer@A.valueOf( 2@A ) );

            // overload resolution
            f5( Integer@A.valueOf( 2@A ), Integer@A.valueOf( 2@A ) );
            f5( 2@A, 2@A );
            // this is an ambiguous invocation
            // f5( Integer@A.valueOf( 2@A ), 2@A );
        }

        public Integer@A f1( Integer@A i ) { return i; }
        public int@A     f1( int@A     i ) { return i; }

        public void f2( int@A i ) { return; }

        public void f3( Integer@A i ) { }
        public void f3( Integer@B i ) { }

        public void f4( int@A i ) { }
        public void f4( int@B i ) { }

        public void f5( Object@A o1, Object@A o2 ) { }
        public void f5( int@A i1, int@A i2 ) { }
}