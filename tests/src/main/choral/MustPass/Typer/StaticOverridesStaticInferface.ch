package Typer.StaticOverridesStaticInferface;

// JLS 9.4.1.1: Static interface methods are not inherited by implementing
// classes or sub-interfaces. Therefore, a class may declare a static method
// with the same signature as a static interface method without triggering
// an override/access check.

interface Foo@A {
  static int@A hello() { return 1@A; }
}

class StaticOverridesStaticInferface@A implements Foo@A {
  // Even though Foo.hello is public and this method is package-private,
  // this is valid because static interface methods are not inherited.
  static int@A hello() { return 0@A; }
}
