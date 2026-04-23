import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import choral.compiler.HeaderLoader;
import choral.compiler.Typer;
import choral.compiler.TyperOptions;
import choral.compiler.typer.ClassLifter;
import choral.compiler.typer.TaskQueue;
import choral.types.Universe;
import choral.utils.VerbosityLevel;

public class ClassLifterTest {

	@Test
	public void helloWorldTest() throws IOException {
		Universe universe = new Universe();
		TaskQueue taskQueue = new TaskQueue();
		TyperOptions opts = new TyperOptions( VerbosityLevel.WARNINGS );

		// Run the typer to initialize special types like java.lang.Object
		Typer.annotate( List.of(), HeaderLoader.loadStandardProfile().toList(), universe, opts );
		
		ClassLifter classLifter = new ClassLifter(universe, taskQueue, opts);

		classLifter.lookup("java.lang.System");
		classLifter.lookup("java.lang.Object");
		classLifter.lookup("java.lang.Enum");
		classLifter.lookup("java.util.stream.BaseStream");
		classLifter.lookup("java.io.PrintStream");
		classLifter.lookup("java.io.Serializable");
		classLifter.lookup("supplement.HelloWorld");
		classLifter.lookup("java.util.HashMap");
		classLifter.lookup("java.util.Deque");
		classLifter.lookup("java.util.ArrayDeque");
		classLifter.lookup("java.util.Random");
		classLifter.lookup("java.time.chrono.AbstractChronology");
	}
}
