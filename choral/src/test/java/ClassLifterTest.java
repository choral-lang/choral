import choral.ast.CompilationUnit;
import choral.compiler.ClassLifter;
import choral.compiler.HeaderLoader;
import choral.compiler.Typer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class ClassLifterTest {
	@Test
	public void helloWorldTest() throws IOException {
		// Test it out by feeding the CompilationUnit to our old Typer
		CompilationUnit compUnit =
				ClassLifter.liftPackage("supplement.HelloWorld");
		Typer.annotate(
				List.of(),
				Stream.concat(
						Stream.of(compUnit),
						// TODO Right now we need to load the standard profile because Typer complains
						//   that java.lang.Object is missing. This is probably a bug in the header
						//   removal tool?
						HeaderLoader.loadStandardProfile()
				).toList()
		);
	}

	@Test
	public void dayTest() throws IOException {
		// Test it out by feeding the CompilationUnit to our old Typer
		CompilationUnit compUnit =
				ClassLifter.liftPackage("supplement.Day");
		Typer.annotate(
				List.of(),
				Stream.concat(
						Stream.of(compUnit),
						// TODO Right now we need to load the standard profile because Typer complains
						//   that java.lang.Object is missing. This is probably a bug in the header
						//   removal tool?
						HeaderLoader.loadStandardProfile()
				).toList()
		);
	}

	@Test
	public void threadStateTest() throws IOException {
		// Test it out by feeding the CompilationUnit to our old Typer
		CompilationUnit compUnit =
				ClassLifter.liftPackage("java.lang.Thread$State");
		Typer.annotate(
				List.of(),
				Stream.concat(
						Stream.of(compUnit),
						// TODO Right now we need to load the standard profile because Typer complains
						//   that java.lang.Object is missing. This is probably a bug in the header
						//   removal tool?
						HeaderLoader.loadStandardProfile()
				).toList()
		);
	}

	@Test
	public void concurrentTimeUnitTest() throws IOException {
		// Test it out by feeding the CompilationUnit to our old Typer
		CompilationUnit compUnit =
				ClassLifter.liftPackage("java.util.concurrent.TimeUnit");
		Typer.annotate(
				List.of(),
				Stream.concat(
						Stream.of(compUnit),
						// TODO Right now we need to load the standard profile because Typer complains
						//   that java.lang.Object is missing. This is probably a bug in the header
						//   removal tool?
						HeaderLoader.loadStandardProfile()
				).toList()
		);
	}

	@Test
	public void interfaceTest() throws IOException {
		// Test it out by feeding the CompilationUnit to our old Typer
		CompilationUnit compUnit =
				ClassLifter.liftPackage("supplement.testInterface");
		Typer.annotate(
				List.of(),
				Stream.concat(
						Stream.of(compUnit),
						// TODO Right now we need to load the standard profile because Typer complains
						//   that java.lang.Object is missing. This is probably a bug in the header
						//   removal tool?
						HeaderLoader.loadStandardProfile()
				).toList()
		);
	}

	@Test 
	public void interface2Test() throws IOException {
		// Test it out by feeding the CompilationUnit to our old Typer
		CompilationUnit compUnit =
				ClassLifter.liftPackage("supplement.testInterface2");
		CompilationUnit compUnit2 = ClassLifter.liftPackage("supplement.testInterface");
		Typer.annotate(
				List.of(),
				Stream.concat(
						Stream.concat(Stream.of(compUnit), Stream.of(compUnit2)),
						// TODO Right now we need to load the standard profile because Typer complains
						//   that java.lang.Object is missing. This is probably a bug in the header
						//   removal tool?
						HeaderLoader.loadStandardProfile()
				).toList()
		);
	}

	@Test
	public void serializableTest() throws IOException {
		// Test it out by feeding the CompilationUnit to the old Typer
		CompilationUnit compUnit =
				ClassLifter.liftPackage("java.lang.Cloneable");
		Typer.annotate(
				List.of(),
				Stream.concat(
						Stream.of(compUnit),
						// TODO Right now we need to load the standard profile because Typer complains
						//   that java.lang.Object is missing. This is probably a bug in the header
						//   removal tool?
						HeaderLoader.loadStandardProfile()
				).toList()
		);
	}

	// @Test // THIS TEST REQUIRES TYPE PARAMETERS
	// public void mapTest() throws IOException {
	// 	// Test it out by feeding the CompilationUnit to the old Typer
	// 	CompilationUnit compUnit =
	// 			ClassLifter.liftPackage("java.util", "java.util.Map");
	// 	Typer.annotate(
	// 			List.of(),
	// 			Stream.concat(
	// 					Stream.of(compUnit),
	// 					// TODO Right now we need to load the standard profile because Typer complains
	// 					//   that java.lang.Object is missing. This is probably a bug in the header
	// 					//   removal tool?
	// 					HeaderLoader.loadStandardProfile()
	// 			).toList()
	// 	);
	// }


	// @Test // THIS TEST REQUIRES TOOL TO BE ABLE TO CALL ITSELF
	// public void mapTest() throws IOException {
	// 	// Test it out by feeding the CompilationUnit to the old Typer
	// 	CompilationUnit compUnit =
	// 			ClassLifter.liftPackage("java.nio.channels", "java.nio.channels.ReadableByteChannel");
	// 	Typer.annotate(
	// 			List.of(),
	// 			Stream.concat(
	// 					Stream.of(compUnit),
	// 					// TODO Right now we need to load the standard profile because Typer complains
	// 					//   that java.lang.Object is missing. This is probably a bug in the header
	// 					//   removal tool?
	// 					HeaderLoader.loadStandardProfile()
	// 			).toList()
	// 	);
	// }
}
