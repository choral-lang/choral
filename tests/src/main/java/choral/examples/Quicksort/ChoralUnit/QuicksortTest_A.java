package choral.examples.Quicksort.ChoralUnit;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.choralUnit.Assert;
import choral.choralUnit.annotations.Test;
import choral.choralUnit.testUtils.TestUtils_A;
import choral.choralUnit.testUtils.TestUtils_B;
import choral.examples.Quicksort.Quicksort_A;
import choral.lang.Unit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Choreography(role = "A", name = "QuicksortTest")
public class QuicksortTest_A {
  @Test
  public static void test1() {
    ArrayList<Integer> a;
    a = new ArrayList<Integer>();
    a.add(5);
    a.add(7);
    a.add(12);
    a.add(22);
    a.add(1);
    a.add(2);
    a.add(45);
    SymChannel_A<Object> ch_AB;
    ch_AB = TestUtils_A.newLocalChannel("ch_AB", Unit.id);
    SymChannel_B<Object> ch_CA;
    ch_CA = TestUtils_B.newLocalChannel(Unit.id, "ch_CA");
    List<Integer> sorted = new Quicksort_A(ch_AB, Unit.id, ch_CA).sort(a);
    List<Integer> expected = Arrays.asList(1, 2, 5, 7, 12, 22, 45);
    Assert.assertEquals(sorted, expected, "success", "failure");
  }
}
