import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import choral.compiler.HeaderLoader;
import choral.compiler.Typer;
import choral.compiler.TyperOptions;
import choral.compiler.typer.ClassLifter;
import choral.compiler.typer.TaskQueue;
import choral.types.Universe;
import choral.utils.VerbosityLevel;

public class ClassLifterTest {

	@Test
	public void stdlibTest() throws IOException {
		Universe universe = new Universe();
		TaskQueue taskQueue = new TaskQueue();
		TyperOptions opts = new TyperOptions( VerbosityLevel.WARNINGS );

		// Run the typer to initialize special types like java.lang.Object
		Typer.annotate( List.of(), HeaderLoader.loadStandardProfile().toList(), universe, opts );
		
		ClassLifter classLifter = new ClassLifter(universe, taskQueue, opts);

		List< String > expectedTypes = List.of(
				"java.nio.ByteBuffer",
				"java.math.BigInteger",
				"java.security.MessageDigest",
				"java.security.NoSuchAlgorithmException",
				"java.nio.charset.StandardCharsets",
				"java.lang.System",
				"java.lang.Object",
				"java.lang.Enum",
				"java.util.stream.BaseStream",
				"java.io.PrintStream",
				"java.io.Serializable",
				"supplement.HelloWorld",
				"java.util.HashMap",
				"java.util.Deque",
				"java.util.ArrayDeque",
				"java.util.Random",
				"java.time.chrono.AbstractChronology"
		);

		assertAll( expectedTypes.stream().map( typeName -> () ->
				assertTrue( classLifter.lookup( typeName, null ).isPresent(),
						() -> "Expected ClassLifter to find " + typeName )
		) );
		assertFalse( classLifter.lookup( "supplement.DoesNotExist", null ).isPresent() );
	}

	@Test
	public void nestedGenericsTest() throws IOException {
		Universe universe = new Universe();
		TaskQueue taskQueue = new TaskQueue();
		TyperOptions opts = new TyperOptions( VerbosityLevel.WARNINGS );

		Typer.annotate( List.of(), HeaderLoader.loadStandardProfile().toList(), universe, opts );

		ClassLifter classLifter = new ClassLifter( universe, taskQueue, opts );
		assertTrue( classLifter.lookup( "supplement.LiftedConcrete", null ).isPresent() );
		taskQueue.process();
	}
}
