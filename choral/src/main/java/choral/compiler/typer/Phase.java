package choral.compiler.typer;

/**
 * Choral and Java allow you to use a type or a method before it's been declared; it's perfectly
 * valid to implement a method foo on line 10 that uses a method bar on line 20. For
 * this to work, we have to split up typechecking into multiple phases:
 * <ol>
 * <li> Type symbol declarations: Declare all the packages, classes, interfaces, and enums
 * without inspecting their contents.
 * <li> Hierarchy: Check if the inheritance hierarchy is ok. This phase detects inheritance
 * cycles, like when Foo extends Bar and Bar extends Foo.
 * <li> Bounds checks: Check type bounds.
 * <li> Member declarations: Declare fields, methods, and constructors without inspecting their
 * implementations.
 * <li> Member definitions: Typecheck the body of a method or constructor.
 * <li> Member global checks: Check for mutual recursion between constructors.
 * </ol>
 */
public enum Phase {
    TYPE_SYMBOL_DECLARATIONS,
    HIERARCHY,
    BOUND_CHECKS,
    MEMBER_DECLARATIONS,
    MEMBER_DEFINITIONS,
    MEMBER_GLOBAL_CHECKS,
}
