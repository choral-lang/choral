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
@WithDefaultIntVal(23)
@WithBool(is_used=true)
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