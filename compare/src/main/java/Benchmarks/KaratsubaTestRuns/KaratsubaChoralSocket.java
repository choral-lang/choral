package Benchmarks.KaratsubaTestRuns;

import Benchmarks.Choral.Karatsuba.Karatsuba_A;
import Benchmarks.Choral.Karatsuba.Karatsuba_B;
import Benchmarks.Choral.Karatsuba.Karatsuba_C;
import choral.choralUnit.testUtils.TestUtils;
import choral.runtime.SerializerChannel.SerializerChannel_A;
import choral.runtime.SerializerChannel.SerializerChannel_B;
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

public class KaratsubaChoralSocket {

	public static final String filepath = System.getProperty(
			"user.dir" ) + "compare/java/Benchmarks/results/";
	public static final String folder = "choral_socket/";

	public static void main( String[] args ) {
		runBenchmark( true );
	}

	public static void runBenchmark( boolean write ) {
		try {
			List< Path > num_files = Files.list( Path.of( CoupleGenerator.filepath ) ).collect(
					Collectors.toList() );
			int i = 0;
			ExecutorService executors = Executors.newFixedThreadPool( 3 );
			Pair< SerializerChannel_A, SerializerChannel_B > ch_AB = TestUtils.newSocketChannel(
					10000 );
			Pair< SerializerChannel_A, SerializerChannel_B > ch_BC = TestUtils.newSocketChannel(
					20000 );
			Pair< SerializerChannel_A, SerializerChannel_B > ch_CA = TestUtils.newSocketChannel(
					30000 );
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
					long start = System.nanoTime();
					Future< ? > f1 = executors.submit(
							() -> Karatsuba_A.multiply( left, right, ch_AB.left(),
									ch_CA.right() ) );
					Future< ? > f2 = executors.submit(
							() -> Karatsuba_B.multiply( ch_AB.right(), ch_BC.left() ) );
					Future< ? > f3 = executors.submit(
							() -> Karatsuba_C.multiply( ch_BC.right(), ch_CA.left() ) );
					f1.get();
					f2.get();
					f3.get(); // wait for termination of the runnables
					times.add( System.nanoTime() - start );
					Thread.sleep( 75 );
					System.out.println( "done " + i++ );
				}
				if( write ) {
					Files.createDirectories( Path.of( filepath + folder ) );
					FileWriter w = new FileWriter( filepath + folder + "results_" + idx + ".csv" );
					w.write( times.toString() );
					w.close();
				}
			}
			executors.shutdown();
		} catch( IOException | ExecutionException | InterruptedException e ) {
			e.printStackTrace();
		}
	}

}
