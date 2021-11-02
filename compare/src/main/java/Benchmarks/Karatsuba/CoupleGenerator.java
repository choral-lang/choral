package Benchmarks.Karatsuba;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class CoupleGenerator {

	public static final String filepath = System.getProperty( "user.dir" ) + "/src/compare/java/Benchmarks/Karatsuba/numbers/";

	public static void main( String[] args ) {
		// 10^9   10^11    10^13    10^15     10^17     10^19
		int[] series = { 5, 6, 7, 8, 9, 10 };
		int[] power = { 9, 11, 13, 15, 17, 18 };

		for( int idx = 0; idx < series.length; idx++ ) {
			StringBuilder content = new StringBuilder();
			for( int i = 0; i < 1000; i++ ) {
				long left = rnd( series[ idx ] );
				long right = rnd( series[ idx ] );
				long prod = left * right;
				int pow = (int) Math.log10( prod );
				if( prod > 0 && power[ idx ] == pow ) {
					//System.out.println( left + " * " + right + " = " + prod + "(" + pow + ")" );
					content.append( left ).append( "," ).append( right ).append(",").append( left*right ).append( "\n" );
				} else {
					i--;
				}
			}
			try {
				FileWriter w = new FileWriter( filepath + "numbers_" + idx + ".csv" );
				w.write( content.toString() );
				w.close();
			} catch( IOException e ) {
				System.out.println( "An error occurred." );
				e.printStackTrace();
			}
		}

	}

	public static long rnd( int bound ) {
		long b = (long) Math.pow( 10, bound );
		return ThreadLocalRandom.current().nextLong( b / 2, b );
	}


}
