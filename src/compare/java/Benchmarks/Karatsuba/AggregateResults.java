package Benchmarks.Karatsuba;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class AggregateResults {

	public static final String filepath = System.getProperty( "user.dir" )
			+ "/src/compare/java/Benchmarks/Karatsuba/results/";

	public static void main( String[] args ) throws IOException {
		HashMap< String, String > map = new HashMap<>();
		map.put( "sequential", "seq" );
		map.put( "choral_local", "cSetup" );
		map.put( "akka_local", "aSetup" );
//		map.put( "choral_socket", "choralSock" );

		List< String > tiers = List.of( new String[] { "sequential", "choral_local", "akka_local" } );

		for( int tier_idx = 0; tier_idx < 6; tier_idx++ ){
			System.out.println( " - - - TIER: " + tier_idx + " - - - " );
			for( String tier : tiers ){
				String results = Files.readString( Path.of( filepath + tier + "/results_" + tier_idx + ".csv" ) );
				// cleanup from list dump
				results = results.replace( "[", "" ).replace( "]", "" ).replace( " ", "" );
				List< String > list = Arrays.stream( results.split( "," ) ).toList();
				List< Double > list_double_raw = list.stream().mapToDouble(
						Double::parseDouble ).boxed().sorted( Comparator.naturalOrder() ).collect(
						Collectors.toList() );
				double avg = list_double_raw.stream().mapToDouble( i -> i ).average().getAsDouble();
//				double stdev = new StandardDeviation().evaluate( list_double_raw.stream().mapToDouble( i -> i ).toArray() );
				System.out.println( "(" + map.get( tier ) + "," + avg + ")" );
			}
		}



	}

}
