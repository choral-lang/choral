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
import java.util.BitSet;
import java.util.Optional;
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
		Optional < String > opt = Optional.< String >of( "present" );
		boolean present = opt.isPresent();
		UUID uuid = UUID.randomUUID();
		String ustr = uuid.toString();
		BitSet bits = new BitSet();
		bits.set( 0 );
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
