import choral.types.Universe;
import choral.types.World;
import org.junit.jupiter.api.Test;

public class WorldTest {
  @Test
  public void worldCreationTest() {
    Universe testUniverse = new Universe();
    World alice = new World(testUniverse, "Alice");
    assert alice.identifier().equals("Alice");
  }
}
