package choral.unitDecoupler;

interface StringChannel@( A, B ){
  String@A com( String@B x );
  String@B com( String@A x );
}

class C@( A, B ){
  void m( String@A x, String@B y) { }
  void m( String@B y, String@A x) { }
}
