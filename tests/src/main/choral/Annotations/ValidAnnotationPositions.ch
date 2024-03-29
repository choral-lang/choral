package choral;

import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;

@StringAnnotation(greeting="hello")
@IntegerAnnotation(val=23)
@BooleanAnnotation(is_true=true)
@MultipleValuesAndTypesAnnotation(first="yay", second=42)
@DefaultValueAnnotation(1337)
public class ValidAnnotationPositions@A {
	@MethodAnnotation1
	@MethodAnnotation2
	public static void main( ) {
		AnnotationTest@A at = new < String > AnnotationTest@A( "abcde"@A );
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

class MyExtensionBaseClass@A< @TypeParamAnnotation1 @TypeParamAnnotation2 T@A > extends MyBaseClass@A< T > {
	MyExtensionBaseClass() { }

	@Override
	T@A baseMethod( T@A param ) {
		return param;
	}
}

@InterfaceAnnotation1
@InterfaceAnnotation2
interface MyInterface@A< @TypeParamAnnotation1 @TypeParamAnnotation2 K@A extends InterfaceA@A & InterfaceB@A, V@A > {
	@InterfaceMethodAnnotation1
	@InterfaceMethodAnnotation1
	< T@A extends K@A > List@A< T > interface_method( T@A param );
}

@EnumAnnotation1
@EnumAnnotation2
enum Day@A {
	MONDAY,
	TUESDAY,
	WEDNESDAY,
	THURSDAY,
	FRIDAY,

	@Weekend
	SATURDAY,

	@Weekend
	@LastDay
	SUNDAY
}

@ClassAnnotation1
@ClassAnnotation2
class AnnotationTest@A extends MyBaseClass@A< String > implements MyInterface@A< MyBaseClass< String >, String > {
	@FieldAnnotation1
	@FieldAnnotation2
	public String@A strField;

	@OtherFieldAnnotation
	public Day@A enumField;

	@ConstructorAnnotation1
	@ConstructorAnnotation2
	<@TypeParamAnnotation1 @TypeParamAnnotation2 T@A> AnnotationTest( @ParameterAnnotation1 @ParameterAnnotation2 String@A strField ) {
		this.strField = strField;
		this.enumField = Day@A.MONDAY;
	}

	@Override
	@MethodAnnotation
	public < @TypeParamAnnotation1 @TypeParamAnnotation2 T@A extends MyBaseClass@A< String > > List@A< T > interface_method( T@A param ) {
		@LocalVariableDeclarationAndAssignmentAnnotation1
		@LocalVariableDeclarationAndAssignmentAnnotation2
		List@A< T > ls = new ArrayList@A<T>();

		try {
			ls.add( param );
		} catch ( @CatchAnnotation1 @CatchAnnotation2 Exception@A e ) {
			System@A.out.println("Didn't work"@A);
		}
		return ls;
	}

	@Override
	String@A baseMethod( String@A param ) {
		@LocalVariableDeclarationAnnotation1
		@LocalVariableDeclarationAnnotation2
		Integer@A tmp;
		tmp = 4@A;

		@MultiDeclarationAndAssignmentAnnotation1
		@MultiDeclarationAndAssignmentAnnotation2
		Boolean@A bool1, bool2, bool3 = true@A, bool4 = false@A, bool5, bool6 = false@A;

		return param;
	}
}
