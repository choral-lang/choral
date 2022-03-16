interface List@( W )< T@( W1 ) extends List@( W1 )< T > > {
	T@( W ) get( Integer@( W ) i );
	T@( W ) add( T@( W ) t );
	T@( W ) remove();
}

interface TwoWorldList@( W1, W2 )< Q@( W3 ) extends List@( W3 )< Q >, R@( W4 ) > extends List@( W1 )< Q >{
	R@( W2 ) getCopy( Integer@( W2 ) i );
}
