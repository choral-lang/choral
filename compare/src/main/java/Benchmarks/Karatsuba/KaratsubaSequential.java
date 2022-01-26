package Benchmarks.Karatsuba;

import Benchmarks.Karatsuba.Java.Karatsuba;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class KaratsubaSequential {

	public static final String filepath = System.getProperty(
			"user.dir" ) + "/src/compare/java/Benchmarks/Karatsuba/results/";
	public static final String folder = "sequential/";

	public static void main( String[] args ) {
		runBenchmarks( false );
		runBenchmarks( true );
	}

	public static void runBenchmarks( boolean write ) {
		try {
			List< Path > num_files = Files.list( Path.of( CoupleGenerator.filepath ) ).collect(
					Collectors.toList() );
			for( Path numbers : num_files ) {
				int idx = Integer.parseInt(
						numbers.getFileName().toString().split( "numbers_" )[ 1 ].split(
								".csv" )[ 0 ] );
				String[] num_lines = Files.readString( numbers ).split( "\n" );
				List< Long > times = new LinkedList<>();
				for( String line : num_lines ) {
					String[] couple = line.split( "," );
					long left = Long.parseLong( couple[ 0 ] );
					long right = Long.parseLong( couple[ 1 ] );
					long result = Long.parseLong( couple[ 2 ] );
					long start = System.nanoTime();
					long thisResult = Karatsuba.multiply( left, right );
					times.add( System.nanoTime() - start );
					if( thisResult != result ) {
						throw new RuntimeException(
								"The procedure returned an unexpected result, expected: " + result + ", computed: " + thisResult );
					}
				}
				if( write ) {
					Files.createDirectories( Path.of( filepath + folder ) );
					FileWriter w = new FileWriter( filepath + folder + "results_" + idx + ".csv" );
					w.write( times.toString() );
					w.close();
				}
			}
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}

}
