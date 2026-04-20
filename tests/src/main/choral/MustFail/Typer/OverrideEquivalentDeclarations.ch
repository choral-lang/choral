package choral.MustFail.Typer.OverrideEquivalentDeclarations;

// JSL 8.4.2: Classes can't declare override-equivalent signatures
class Point@A {
  int@A x, y;
  void move(int@A dx, int@A dy) { x += dx; y += dy; }
  void move(int@A dx, int@A dy) { x += dx; y += dy; } //!already defined
}