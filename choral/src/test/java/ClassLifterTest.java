import choral.compiler.typer.ClassLifter;
import choral.compiler.HeaderLoader;
import choral.compiler.Typer;
import choral.compiler.TyperOptions;
import choral.types.HigherClassOrInterface;
import choral.types.Universe;
import choral.utils.VerbosityLevel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ClassLifterTest {

	@Test
	public void helloWorldTest() throws IOException {
		Universe universe = new Universe();
		
		// Stream<HigherClassOrInterface> baseStream = ClassLifter.liftPackage
		// 	("java.util.stream.BaseStream", universe.rootPackage());

		// Run the typer to initialize special types like java.lang.Object
		Typer.annotate( List.of(), HeaderLoader.loadStandardProfile().toList(), universe, new TyperOptions( VerbosityLevel.WARNINGS ) );
		
		ClassLifter classLifter = new ClassLifter(universe);

		assert(classLifter.liftClassOrInterface("java.lang.Object").isPresent());
		assert(classLifter.liftClassOrInterface("java.lang.Enum").isPresent());

		assert(classLifter.liftClassOrInterface("java.util.stream.BaseStream").isPresent());
		
		assert(classLifter.liftClassOrInterface("java.io.PrintStream").isPresent());

		assert(classLifter.liftClassOrInterface("java.io.Serializable").isPresent());
		
		// Stream<HigherClassOrInterface> compUnit = ClassLifter.liftPackage(
		// 	"supplement.HelloWorld", universe.rootPackage());



		// Stream<HigherClassOrInterface> intermediary = Stream.of(enuM, object, serializable)
		// 										.flatMap(i -> i);
		// List<CompilationUnit> finalList = Stream.concat(intermediary, compUnit).toList();
		// List<CompilationUnit> finalList = Stream.concat(enuM, Stream.concat(Stream.concat(serializable, object), compUnit)).toList();

		// Print the lifted CompilationUnits for debugging
		// PrettyPrinterVisitor ppv = new PrettyPrinterVisitor();
		// finalList.forEach(cu -> {
		// 	if(cu.packageDeclaration().get().equalsIgnoreCase("java.lang")){
		// 		System.out.println(cu.accept(ppv));
		// 	} 
		// });

		// Typer.annotate(
		// 		List.of(),
		// 		// compUnit.toList()
		// 		finalList,
		// 		// Stream.concat(
		// 		// 		compUnit,
		// 		// // 		TODO Right now we need to load the standard profile because Typer complains
		// 		// // 		  that java.lang.Object is missing. This is probably a bug in the header
		// 		// // 		  removal tool?
		// 		// 		HeaderLoader.loadAlternateProfile()
		// 		// ).toList()
		// 		new TyperOptions( VerbosityLevel.WARNINGS )
		// );
		// ClassLifter.clearTrackedCompilationUnits();
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
