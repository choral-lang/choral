package choral.MustPass.ClassLifter.JavaStdLibImports;

import choral.annotations.Choreography;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.lang.Boolean;
import java.lang.Byte;
import java.lang.Double;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Math;
import java.lang.Number;
import java.lang.Short;
import java.lang.StringBuffer;
import java.lang.StringBuilder;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

@Choreography( role = "A", name = "JavaStdLibImports" )
class JavaStdLibImports_A {
	public void test() {
		Boolean b = Boolean.TRUE;
		Integer i = Integer.valueOf( 42 );
		Long l = Long.valueOf( 100 );
		Double d = Double.valueOf( 3.14 );
		Float f = Float.valueOf( "1.0" );
		Byte by = Byte.valueOf( "" + 1 );
		Short sh = Short.valueOf( "" + 1 );
		Number n = i;
		String s = "hello";
		StringBuilder sb = new StringBuilder( "init" );
		StringBuffer sbuf = new StringBuffer( "init" );
		int abs = Math.abs( -1 );
		int max = Math.max( 1, 2 );
		int hash = Objects.hashCode( s );
		String istr = Integer.toString( 42 );
		String lstr = Long.toString( 100 );
		List < String > list = new ArrayList < String >();
		list.add( "a" );
		Set < String > set = new HashSet < String >();
		set.add( "b" );
		Map < String, Integer > map = new HashMap < String, Integer >();
		Deque < String > deque = new ArrayDeque < String >();
		deque.addFirst( "c" );
		deque.addLast( "d" );
		Optional < String > opt = Optional.< String >of( "present" );
		boolean present = opt.isPresent();
		UUID uuid = UUID.randomUUID();
		String ustr = uuid.toString();
		BitSet bits = new BitSet();
		bits.set( 0 );
		StringJoiner sj = new StringJoiner( "," );
		sj.add( "x" );
		BigInteger bi = BigInteger.ZERO;
		BigInteger bi2 = bi.add( BigInteger.ONE );
		BigDecimal bd = BigDecimal.ONE;
		BigDecimal bd2 = bd.multiply( BigDecimal.TEN );
		Instant now = Instant.now();
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		LocalDateTime dt = LocalDateTime.now();
		Duration dur = Duration.ofSeconds( 60 );
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StringWriter sw = new StringWriter();
	}

}
