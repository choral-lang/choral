import org.junit.jupiter.api.Test;

import choral.types.Universe;
import choral.types.World;

public class WhiteBoxTests {
    @Test
    public void worldCreationTest () {
        Universe testUniverse = new Universe();
		World lmao = new World(testUniverse, "lmao");
		assert lmao.identifier().equals("lmao");
    }
}
