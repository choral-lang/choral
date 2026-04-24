import choral.compiler.HeaderLoader;
import choral.compiler.Typer;
import choral.compiler.TyperOptions;
import choral.compiler.typer.ClassLifter;
import choral.compiler.typer.TaskQueue;
import choral.types.Universe;
import choral.utils.VerbosityLevel;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ClassLifterTest {

  @Test
  public void helloWorldTest() throws IOException {
    Universe universe = new Universe();
    TaskQueue taskQueue = new TaskQueue();
    TyperOptions opts = new TyperOptions(VerbosityLevel.WARNINGS);

    // Run the typer to initialize special types like java.lang.Object
    Typer.annotate(List.of(), HeaderLoader.loadStandardProfile().toList(), universe, opts);

    ClassLifter classLifter = new ClassLifter(universe, taskQueue, opts);

    classLifter.lookup("java.lang.System", null);
    classLifter.lookup("java.lang.Object", null);
    classLifter.lookup("java.lang.Enum", null);
    classLifter.lookup("java.util.stream.BaseStream", null);
    classLifter.lookup("java.io.PrintStream", null);
    classLifter.lookup("java.io.Serializable", null);
    classLifter.lookup("supplement.HelloWorld", null);
    classLifter.lookup("java.util.HashMap", null);
    classLifter.lookup("java.util.Deque", null);
    classLifter.lookup("java.util.ArrayDeque", null);
    classLifter.lookup("java.util.Random", null);
    classLifter.lookup("java.time.chrono.AbstractChronology", null);
  }
}
