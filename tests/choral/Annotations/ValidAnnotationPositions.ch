package choral;

import java.util.ArrayList;
import java.util.List;

@StringAnnotation(greeting="hello")
@IntegerAnnotation(val=23)
@BooleanAnnotation(is_true=true)
@MultipleValuesAndTypesAnnotation(first="yay", second=42)
@DefaultValueAnnotation(1337)
public class ValidAnnotationPositions@A {
    @MethodAnnotation
	public static void main( ) {
		AnnotationTest@A at = new AnnotationTest@A( "abcde"@A );
		List@A< MyExtensionBaseClass< String > > ls = at.< MyExtensionBaseClass< String > >interface_method( new MyExtensionBaseClass@A< String >() );
		System@A.out.println( ls.get( 0@A ).baseMethod( "yay"@A ) );
	}
}

interface InterfaceA@A { }

interface InterfaceB@A { }

class MyBaseClass@A< T@A > implements InterfaceA@A, InterfaceB@A {
	T@A baseMethod( T@A param ) {
		return param;
	}
}

class MyExtensionBaseClass@A< T@A > extends MyBaseClass@A< T > {
	MyExtensionBaseClass() { }

	@Override
	T@A baseMethod( T@A param ) {
		return param;
	}
}

@InterfaceAnnotation
interface MyInterface@A< K@A extends InterfaceA@A & InterfaceB@A, V@A > {
    @InterfaceMethodAnnotation
	< T@A extends K@A > List@A< T > interface_method( T@A param );
}

@EnumAnnotation
enum Day@A {
	MONDAY,
	TUESDAY,
	WEDNESDAY,
	THURSDAY,
	FRIDAY,
	SATURDAY,
	SUNDAY
}

@ClassAnnotation
class AnnotationTest@A extends MyBaseClass@A< String > implements MyInterface@A< MyBaseClass< String >, String > {

	public String@A strField;
	public Day@A enumField;

    @ConstructorAnnotation
	AnnotationTest( String@A strField ) {
		this.strField = strField;
		this.enumField = Day@A.MONDAY;
	}

	@Override
	@MethodAnnotation
	public < T@A extends MyBaseClass@A< String > > List@A< T > interface_method( T@A param ) {
		List@A< T > ls = new ArrayList@A<T>();
		ls.add( param );
		return ls;
	}

	@Override
	String@A baseMethod( String@A param ) {
		Integer@A tmp = 4@A;
		return param;
	}
}