package Benchmarks.Karatsuba;

import Benchmarks.Karatsuba.Choral.Karatsuba_A;
import Benchmarks.Karatsuba.Choral.Karatsuba_B;
import Benchmarks.Karatsuba.Choral.Karatsuba_C;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.choralUnit.testUtils.TestUtils;
import choral.utils.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class KaratsubaChoralLocal {

	public static final String filepath = System.getProperty(
			"user.dir" ) + "/src/compare/java/Benchmarks/Karatsuba/results/";
	public static final String folder = "choral_local/";

	public static void main( String[] args ) {
		runBenchmarks( false );
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
				ExecutorService executors = Executors.newFixedThreadPool( 3 );
				for( String line : num_lines ) {
					String[] couple = line.split( "," );
					long left = Long.parseLong( couple[ 0 ] );
					long right = Long.parseLong( couple[ 1 ] );
					Pair< SymChannel_A< Object >, SymChannel_B< Object > > ch_AB = TestUtils.newLocalChannel(
							"ch_AB" );
					Pair< SymChannel_A< Object >, SymChannel_B< Object > > ch_BC = TestUtils.newLocalChannel(
							"ch_BC" );
					Pair< SymChannel_A< Object >, SymChannel_B< Object > > ch_CA = TestUtils.newLocalChannel(
							"ch_CA" );
					long start = System.nanoTime();
					Future< ? > f1 = executors.submit( () -> {
						Karatsuba_A.multiply( left, right, ch_AB.left(), ch_CA.right() );
					} );
					executors.submit( () -> {
						Karatsuba_B.multiply( ch_AB.right(), ch_BC.left() );
					} );
					executors.submit( () -> {
						Karatsuba_C.multiply( ch_BC.right(), ch_CA.left() );
					} );
					f1.get();
					times.add( System.nanoTime() - start );
				}
				if( write ){
					Files.createDirectories( Path.of( filepath + folder ) );
					FileWriter w = new FileWriter( filepath + folder + "results_" + idx + ".csv" );
					w.write( times.toString() );
					w.close();
				}
				executors.shutdown();
			}
		} catch( IOException | ExecutionException | InterruptedException e ) {
			e.printStackTrace();
		}
	}

}
