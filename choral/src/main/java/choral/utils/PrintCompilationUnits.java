package choral.utils;

import java.util.Collection;
import java.util.stream.Collectors;

import choral.ast.CompilationUnit;
import choral.types.GroundDataType;

/**
 * Some useful methods to print out lists of CompilationUnits.
 */
public class PrintCompilationUnits {
    
	public static void printSourceUnits( Collection< CompilationUnit > sourceUnits ){
		System.out.println( "Source units: " );
		printCompilationUnits(sourceUnits);
	}

	public static void printHeaderUnits( Collection< CompilationUnit > headerUnits ){
		System.out.println( "Header units: " );
		printCompilationUnits(headerUnits);
	}

	public static void printCompilationUnits( Collection< CompilationUnit > compilationUnits ){
		compilationUnits.stream().forEach( compilationUnit -> {
			String string = "\u001B[34mPrimaty type:\u001B[0m " + compilationUnit.primaryType() + " at " + compilationUnit.position();
			printWithIndentation(string, 1);
			string = blue("Package: ") + (compilationUnit.packageDeclaration().isPresent()?compilationUnit.packageDeclaration().get():"None");
			printWithIndentation(string, 2);
			string = blue("Imports: ") + compilationUnit.imports().stream().map( im -> im.name() ).collect( Collectors.toList() );
			printWithIndentation(string, 2);
			string = blue("Interfaces: ") + compilationUnit.interfaces().stream().map( in -> in.name() ).collect( Collectors.toList() );
			printWithIndentation(string, 2);
			string = blue("Classes: ") + compilationUnit.classes().stream().map( cl -> cl.name() ).collect( Collectors.toList() );
			printWithIndentation(string, 2);
			string = blue("Class methods: ") + compilationUnit.classes().stream().map( cl -> cl.methods().stream().map( meth -> meth.toString() ).collect( Collectors.toList() ) ).collect( Collectors.toList() );
			printWithIndentation(string, 3);
			string = blue("Enums: ") + compilationUnit.enums().stream().map( en -> en.name() ).collect( Collectors.toList() );
			printWithIndentation(string, 2);
		} );
	}

	public static String blue( String string ){
		return "\u001B[34m" + string + "\u001B[0m";
	}

	public static void printWithIndentation(String string, int indentation){
		printWithIndentation(string, "    ".repeat(indentation), 120);
	}

	public static void printWithIndentation(String string, String indentation, int maxwidth){
		String[] words = string.split( " " );
		StringBuilder line = new StringBuilder( indentation );

		for( String word : words ){
			if( line.length() + word.length() >= maxwidth ){
				System.out.println( line.toString() );
				line = new StringBuilder( indentation + "    " );
			}
			line.append( " " + word );
		}
		if( line.length() > 0 ){
			System.out.println( line.toString() );
		}
	}

	public static void printWorldDependenciesAndChannels(Collection<CompilationUnit> units){
		units.forEach( sourceUnit -> {
			// sourceUnit
			sourceUnit.classes().forEach( cls -> {
				// class
				cls.methods().forEach( method -> {
					// method 
					System.out.println( "Dependencies for method " + blue(method.signature().typeAnnotation().get().toString()) );
					if( !method.signature().typeAnnotation().get().worldDependencies().isEmpty() ){
						method.signature().typeAnnotation().get().worldDependencies().forEach( (world, depenList) -> {
							// world dependencies
							System.out.println( "\tRole " + world + " needs" );
							depenList.forEach( dependency -> System.out.println( "\t\t" + dependency.left() + " of type " + ((GroundDataType)dependency.left().typeAnnotation().get()).toString() + " at " + dependency.left().position() ) );
						} );
					}else{
						System.out.println( "\tNone" );
					}
					System.out.println( "Channels for method " + blue(method.signature().typeAnnotation().get().toString()) );
					if( !method.signature().typeAnnotation().get().channels().isEmpty() ){
						method.signature().typeAnnotation().get().channels().forEach( ch -> System.out.println( "\t" + ch ) );
					}else{
						System.out.println( "\tNone" );
					}
				});
			});
		} );
	}

	public static void printWorldDependencies(Collection<CompilationUnit> units){
		units.forEach( sourceUnit -> {
			// sourceUnit
			sourceUnit.classes().forEach( cls -> {
				// class
				cls.methods().forEach( method -> {
					// method 
					System.out.println( "Dependencies for method " + blue(method.signature().typeAnnotation().get().toString()) );
					if( !method.signature().typeAnnotation().get().worldDependencies().isEmpty() ){
						method.signature().typeAnnotation().get().worldDependencies().forEach( (world, depenList) -> {
							// world dependencies
							System.out.println( "\tRole " + world + " needs" );
							depenList.forEach( dependency -> System.out.println( "\t\t" + dependency.left() + " of type " + ((GroundDataType)dependency.left().typeAnnotation().get()).toString() + " at " + dependency.left().position() ) );
						} );
					}else{
						System.out.println( "\t\tNone" );
					}
				});
			});
		} );
	}

	public static void printChannels(Collection<CompilationUnit> units){
		units.forEach( sourceUnit -> {
			// sourceUnit
			sourceUnit.classes().forEach( cls -> {
				// class
				cls.methods().forEach( method -> {
					// method 
					System.out.println( "Channels for method " + blue(method.signature().typeAnnotation().get().toString()) );
					if( !method.signature().typeAnnotation().get().channels().isEmpty() ){
						method.signature().typeAnnotation().get().channels().forEach( ch -> System.out.println( "\t" + ch ) );
					}else{
						System.out.println( "\t\tNone" );
					}
				});
			});
		} );
	}

}
