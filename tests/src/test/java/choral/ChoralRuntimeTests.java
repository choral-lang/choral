package choral;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import choral.choralUnit.ChoralUnit;

public class ChoralRuntimeTests {

    @ParameterizedTest
    @ValueSource(strings = {
            "VitalsStreamingTest",
            "MergesortTest",
            "QuicksortTest",
            "KaratsubaTest"
    })
    public void unitTests(String name) {
        assertDoesNotThrow(() -> {
            ChoralUnit.main(new String[] { name });
        });
    }

}
