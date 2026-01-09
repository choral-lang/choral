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
				ClassLifter.liftPackage("supplement", "supplement.HelloWorld");
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
				ClassLifter.liftPackage("supplement", "supplement.Day");
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
				ClassLifter.liftPackage("java.lang", "java.lang.Thread$State");
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
				ClassLifter.liftPackage("java.util.concurrent", "java.util.concurrent.TimeUnit");
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
}
