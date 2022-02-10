@AnnotatedInterface
interface I@A {}

@AnnotatedEnum
enum Time@A {
    DAY, NIGHT
}

@AnnotatedClass
class BaseClass@A {
    @AnnotatedMethod
    void baseMethod(String@A param) { }
}

@Deprecated(since="4.2")
@WithDefaultVal(23)
@WithMultipleValues(name="Test", age=25, is_developer=true)
class AnnotationTest@A extends BaseClass@A implements I@A {

    public String@A strField;
    public Time@A enumField;

    AnnotationTest() {
        strField = "test"@A;
        enumField = Time@A.DAY;
    }

    @Override
    void baseMethod(String@A param) {
        Integer@A tmp = 4@A;
    }
}