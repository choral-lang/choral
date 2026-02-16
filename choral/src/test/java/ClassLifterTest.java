import choral.ast.CompilationUnit;
import choral.compiler.ClassLifter;
import choral.compiler.Typer;
import choral.compiler.TyperOptions;
import choral.utils.VerbosityLevel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class ClassLifterTest {
	// Dan: At time of writing, these tests take several seconds to run and still fail. We'll
	// revisit these when the class lifter is better integrated with the typer.

	@Test
	public void helloWorldTest() throws IOException {
		Stream< CompilationUnit > object = ClassLifter.liftPackage("java.lang.Object");
		Stream<CompilationUnit> doubleC = ClassLifter.liftPackage("java.lang.Double");
		Stream<CompilationUnit> intC = ClassLifter.liftPackage("java.lang.Integer");
		Stream<CompilationUnit> longC = ClassLifter.liftPackage("java.lang.Long");
		Stream<CompilationUnit> serializable = ClassLifter.liftPackage("java.io.Serializable");
		Stream<CompilationUnit> enuM = ClassLifter.liftPackage("java.lang.Enum");
		Stream<CompilationUnit> compUnit = ClassLifter.liftPackage("supplement.HelloWorld");
		Stream<CompilationUnit> intermediary = Stream.of(enuM, object, serializable, doubleC, intC, longC)
												.flatMap(i -> i);
		List<CompilationUnit> finalList = Stream.concat(intermediary, compUnit).toList();
		// List<CompilationUnit> finalList = Stream.concat(enuM, Stream.concat(Stream.concat(serializable, object), compUnit)).toList();

		Typer.annotate(
				List.of(),
				// compUnit.toList()
				finalList,
				// Stream.concat(
				// 		compUnit,
				// // 		TODO Right now we need to load the standard profile because Typer complains
				// // 		  that java.lang.Object is missing. This is probably a bug in the header
				// // 		  removal tool?
				// 		HeaderLoader.loadAlternateProfile()
				// ).toList()
				new TyperOptions( VerbosityLevel.WARNINGS )
		);
		ClassLifter.clearTrackedCompilationUnits();
	}

	// @Test
	// public void dayTest() throws IOException {
	// 	// Test it out by feeding the CompilationUnit to our old Typer
	// 	Stream<CompilationUnit> compUnit =
	// 			ClassLifter.liftPackage("supplement.Day");
	// 	Typer.annotate(
	// 			List.of(),
	// 			Stream.concat(
	// 					compUnit,
	// 					// TODO Right now we need to load the standard profile because Typer complains
	// 					//   that java.lang.Object is missing. This is probably a bug in the header
	// 					//   removal tool?
	// 					HeaderLoader.loadStandardProfile()
	// 			).toList()
	// 	);
	// 	ClassLifter.clearTrackedCompilationUnits();
	// }

	// @Test
	// public void threadStateTest() throws IOException {
	// 	// Test it out by feeding the CompilationUnit to our old Typer
	// 	Stream<CompilationUnit> compUnit =
	// 			ClassLifter.liftPackage("java.lang.Thread$State");

	// 	assertFalse(compUnit.findAny().isPresent(), "A CompilationUnit was created for an inner class. Choral does not support this");
	// 	ClassLifter.clearTrackedCompilationUnits();
	// }

	// @Test
	// public void concurrentTimeUnitTest() throws IOException {
	// 	// Test it out by feeding the CompilationUnit to our old Typer
	// 	Stream<CompilationUnit> compUnit =
	// 			ClassLifter.liftPackage("java.util.concurrent.TimeUnit");
	// 	Typer.annotate(
	// 			List.of(),
	// 			Stream.concat(
	// 					compUnit,
	// 					// TODO Right now we need to load the standard profile because Typer complains
	// 					//   that java.lang.Object is missing. This is probably a bug in the header
	// 					//   removal tool?
	// 					HeaderLoader.loadStandardProfile()
	// 			).toList()
	// 	);
	// 	ClassLifter.clearTrackedCompilationUnits();
	// }

	// @Test
	// public void interfaceTest() throws IOException {
	// 	// Test it out by feeding the CompilationUnit to our old Typer
	// 	Stream<CompilationUnit> compUnit =
	// 			ClassLifter.liftPackage("supplement.testInterface");
	// 	Typer.annotate(
	// 			List.of(),
	// 			Stream.concat(
	// 					compUnit,
	// 					// TODO Right now we need to load the standard profile because Typer complains
	// 					//   that java.lang.Object is missing. This is probably a bug in the header
	// 					//   removal tool?
	// 					HeaderLoader.loadStandardProfile()
	// 			).toList()
	// 	);
	// 	ClassLifter.clearTrackedCompilationUnits();
	// }

	// @Test 
	// public void interface2Test() throws IOException {
	// 	// Test it out by feeding the CompilationUnit to our old Typer
	// 	Stream<CompilationUnit> compUnit =
	// 			ClassLifter.liftPackage("supplement.testInterface2");
	// 	Stream<CompilationUnit> compUnit2 = ClassLifter.liftPackage("supplement.testInterface");
	// 	Typer.annotate(
	// 			List.of(),
	// 			Stream.concat(
	// 					Stream.concat(compUnit, compUnit2),
	// 					// TODO Right now we need to load the standard profile because Typer complains
	// 					//   that java.lang.Object is missing. This is probably a bug in the header
	// 					//   removal tool?
	// 					HeaderLoader.loadStandardProfile()
	// 			).toList()
	// 	);
	// 	ClassLifter.clearTrackedCompilationUnits();
	// }

	// @Test
	// public void serializableTest() throws IOException {
	// 	// Test it out by feeding the CompilationUnit to the old Typer
	// 	Stream<CompilationUnit> compUnit =
	// 			ClassLifter.liftPackage("java.lang.Cloneable");
	// 	Typer.annotate(
	// 			List.of(),
	// 			Stream.concat(
	// 					compUnit,
	// 					// TODO Right now we need to load the standard profile because Typer complains
	// 					//   that java.lang.Object is missing. This is probably a bug in the header
	// 					//   removal tool?
	// 					HeaderLoader.loadStandardProfile()
	// 			).toList()
	// 	);
	// 	ClassLifter.clearTrackedCompilationUnits();
	// }

	// @Test // THIS TEST REQUIRES TOOL TO BE ABLE TO CALL ITSELF
	// public void InterfaceSetTest() throws IOException {
	// // 	Test it out by feeding the CompilationUnit to the old Typer
	// 	Stream<CompilationUnit> compUnit =
	// 			ClassLifter.liftPackage("java.lang.Iterable");
	// 	Typer.annotate(
	// 			List.of(),
	// 			Stream.concat(
	// 					compUnit,
	// // 					TODO Right now we need to load the standard profile because Typer complains
	// // 					  that java.lang.Object is missing. This is probably a bug in the header
	// // 					  removal tool?
	// 					HeaderLoader.loadStandardProfile()
	// 			).toList()
	// 	);
	// }

	// @Test // THIS TEST REQUIRES TOOL TO BE ABLE TO CALL ITSELF
	// public void InterfaceSetTest() throws IOException {
	// 	// Test it out by feeding the CompilationUnit to the old Typer
	// 	Stream<CompilationUnit> compUnit =
	// 			ClassLifter.liftPackage("java.util.Set");
	// 	Typer.annotate(
	// 			List.of(),
	// 			Stream.concat(
	// 					compUnit,
	// 					// TODO Right now we need to load the standard profile because Typer complains
	// 					//   that java.lang.Object is missing. This is probably a bug in the header
	// 					//   removal tool?
	// 					HeaderLoader.loadStandardProfile()
	// 			).toList()
	// 	);
	// }

	// @Test // THIS TEST REQUIRES TYPE PARAMETERS
	// public void mapTest() throws IOException {
	// 	// Test it out by feeding the CompilationUnit to the old Typer
	// 	Stream<CompilationUnit> compUnit =
	// 			ClassLifter.liftPackage("java.util.Map");
	// 	Typer.annotate(
	// 			List.of(),
	// 			Stream.concat(
	// 					compUnit,
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
