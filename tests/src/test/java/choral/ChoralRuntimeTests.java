package choral;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import choral.choralUnit.ChoralUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ChoralRuntimeTests {

  @ParameterizedTest
  @ValueSource(strings = {"VitalsStreamingTest", "MergesortTest", "QuicksortTest", "KaratsubaTest"})
  public void unitTests(String name) {
    assertDoesNotThrow(
        () -> {
          ChoralUnit.main(new String[] {name});
        });
  }
}
