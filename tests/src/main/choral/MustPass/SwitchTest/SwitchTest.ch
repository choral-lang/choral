package choral.MustPass.SwitchTest;

public class SwitchTest@(A,B) {

    int@A m1(Choice@A c) {
        switch(c) {
            case FIRST -> { return 1@A; }
            case SECOND -> { return 2@A; }
            case THIRD -> { return 3@A; }
        }
        return 0@A;
    }

    int@A m2(Choice@A c) {
        switch(c) {
            case FIRST -> { return 1@A; }
            case SECOND -> { return 2@A; }
            default -> { return 3@A; }
        }
    }

    int@A m3(Choice@A c) {
        switch(c) {
            case FIRST -> { return 1@A; }
            default -> { return 0@A; }
        }
    }

}

enum Choice@A{
    FIRST, SECOND, THIRD
}
