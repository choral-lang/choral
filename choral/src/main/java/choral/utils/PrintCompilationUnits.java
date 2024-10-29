package choral.utils;

import java.util.Collection;
import java.util.stream.Collectors;

import choral.ast.CompilationUnit;

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
			string = "\u001B[34mPackage:\u001B[0m " + (compilationUnit.packageDeclaration().isPresent()?compilationUnit.packageDeclaration().get():"None");
			printWithIndentation(string, 2);
			string = "\u001B[34mImports:\u001B[0m " + compilationUnit.imports().stream().map( im -> im.name() ).collect( Collectors.toList() );
			printWithIndentation(string, 2);
			string = "\u001B[34mInterfaces:\u001B[0m " + compilationUnit.interfaces().stream().map( in -> in.name() ).collect( Collectors.toList() );
			printWithIndentation(string, 2);
			string = "\u001B[34mClasses:\u001B[0m " + compilationUnit.classes().stream().map( cl -> cl.name() ).collect( Collectors.toList() );
			printWithIndentation(string, 2);
			string = "\u001B[34mEnums:\u001B[0m " + compilationUnit.enums().stream().map( en -> en.name() ).collect( Collectors.toList() );
			printWithIndentation(string, 2);
		} );
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
}
