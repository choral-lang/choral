package Benchmarks.KaratsubaTestRuns;

import Benchmarks.Akka.Karatsuba.Karatsuba;
import Benchmarks.Akka.Karatsuba.KaratsubaMessage;
import Benchmarks.Akka.Karatsuba.KaratsubaOperation;
import Benchmarks.Akka.Karatsuba.KaratsubaRootRequest;
import akka.actor.typed.ActorSystem;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class KaratsubaAkkaLocal {

	public static final String filepath = System.getProperty( "user.dir" )
			+ "compare/java/Benchmarks/results/";
	public static final String folder = "akka_local/";

	public static void main( String[] args ) {
		runBenchmarks( false );
		runBenchmarks( true );
	}

	public static void runBenchmarks( boolean write ) {
		try {
			List< Path > num_files = Files.list( Path.of( CoupleGenerator.filepath ) ).collect(
					Collectors.toList() );
			int i = 1;
			for( Path numbers : num_files ) {
				int idx = Integer.parseInt(
						numbers.getFileName().toString()
								.split( "numbers_" )[ 1 ]
								.split( ".csv" )[ 0 ] );
				String[] num_lines = Files.readString( numbers ).split( "\n" );
				List< Long > times = new LinkedList<>();
				for( String line : num_lines ) {
					String[] couple = line.split( "," );
					long left = Long.parseLong( couple[ 0 ] );
					long right = Long.parseLong( couple[ 1 ] );
					long result = Long.parseLong( couple[ 2 ] );
					long start = System.nanoTime();
					ActorSystem< KaratsubaMessage > system =
							ActorSystem.create( Karatsuba.create(), "KaratsubaTest" );
					KaratsubaRootRequest request = new KaratsubaRootRequest(
							new KaratsubaOperation( left, right ) );
					system.tell( request );
					request.resultFuture().get();
					system.getWhenTerminated();
					system.terminate();
					times.add( System.nanoTime() - start );
					if( !request.resultFuture().get().equals( result ) ) {
//						System.out.println( "azz " + request.resultFuture().get() + " instead of " + result );
						throw new RuntimeException(
								"The procedure returned an unexpected result, expected: " + result + ", computed: " + request.resultFuture().get() );
					} else {
						System.out.println( "done " + i++ );
					}
				}
				if( write ) {
					Files.createDirectories( Path.of( filepath + folder ) );
					FileWriter w = new FileWriter( filepath + folder + "results_" + idx + ".csv" );
					w.write( times.toString() );
					w.close();
				}
			}
		} catch( IOException | ExecutionException | InterruptedException e ) {
			e.printStackTrace();
		}
	}

}
