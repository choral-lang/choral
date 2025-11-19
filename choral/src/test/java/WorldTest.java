import org.junit.jupiter.api.Test;

import choral.types.Universe;
import choral.types.World;

public class WorldTest {
    @Test
    public void worldCreationTest() {
        Universe testUniverse = new Universe();
		World alice = new World(testUniverse, "Alice");
		assert alice.identifier().equals("Alice");
    }
}
