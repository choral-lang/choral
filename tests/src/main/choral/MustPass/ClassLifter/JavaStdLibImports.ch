package choral.MustPass.ClassLifter.JavaStdLibImports;

// Stress-test for ClassLifter: imports common classes, interfaces, and
// enums from the Java standard library to verify that reflective lifting
// handles inheritance hierarchies, generics, and modifier combinations.
//
// We primarily test that these types can be lifted and used in type
// positions (fields, locals, casts). We avoid constructor calls for types
// whose lifted constructors may not match Choral's world-parameter model.

// --- java.lang ---
import java.lang.Boolean;
import java.lang.Byte;
import java.lang.Character;
import java.lang.Short;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Float;
import java.lang.Double;
import java.lang.Number;
import java.lang.Math;
import java.lang.StrictMath;
import java.lang.StringBuilder;
import java.lang.StringBuffer;
import java.lang.Thread;
import java.lang.Runnable;
import java.lang.Comparable;
import java.lang.Iterable;
import java.lang.AutoCloseable;
import java.lang.Cloneable;
import java.lang.Throwable;
import java.lang.Exception;
import java.lang.RuntimeException;
import java.lang.Error;
import java.lang.StackTraceElement;
import java.lang.ClassLoader;
import java.lang.ProcessBuilder;
import java.lang.Runtime;
import java.lang.System;
import java.lang.Void;

// --- java.io ---
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.Serializable;
import java.io.Closeable;
import java.io.Flushable;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

// --- java.util ---
import java.util.List;
import java.util.ArrayList;
// LinkedList, Vector, Stack omitted: ClassLifter gives them addAll(Collection)
// but List.chh declares addAll(List), causing a return-type clash.
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.Queue;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Collections;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Date;
import java.util.Calendar;
import java.util.UUID;
import java.util.BitSet;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Spliterator;
import java.util.StringJoiner;

// --- java.util.concurrent ---
import java.util.concurrent.ConcurrentHashMap;
// CopyOnWriteArrayList omitted: same addAll(List) clash as LinkedList/Vector.
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

// --- java.util.regex ---
import java.util.regex.Pattern;
import java.util.regex.Matcher;

// --- java.math ---
import java.math.BigInteger;
import java.math.BigDecimal;

// --- java.net ---
import java.net.URL;
import java.net.URI;

// --- java.nio ---
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.file.Path;

// --- java.time ---
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.Duration;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;

class JavaStdLibImports@( A, B ) {
	public void test() {
		// java.lang - boxed primitives and core types
		Boolean@A b = Boolean@A.TRUE;
		Integer@A i = Integer@A.valueOf( 42@A );
		Long@A l = Long@A.valueOf( 100@A );
		Double@A d = Double@A.valueOf( 3.14@A );
		// Float.valueOf(float) requires a float literal; 1.0@A is a double.
		// Using String overload instead to avoid a pre-existing Typer crash
		// on float literals.
		Float@A f = Float@A.valueOf( "1.0"@A );
		Byte@A by = Byte@A.valueOf( ""@A + 1@A );
		Short@A sh = Short@A.valueOf( ""@A + 1@A );
		Number@A n = i;
		String@A s = "hello"@A;
		StringBuilder@A sb = new StringBuilder@A( "init"@A );
		StringBuffer@A sbuf = new StringBuffer@A( "init"@A );

		// java.lang - static method calls
		//int@A abs = Math@A.abs( -1@A );
		//int@A max = Math@A.max( 1@A, 2@A );
		//int@A hash = Objects@A.hashCode( s );
		//String@A istr = Integer@A.toString( 42@A );
		//String@A lstr = Long@A.toString( 100@A );

		// java.util - collections via static factory / constructors
		//List@A<String> list = new ArrayList@A<String>();
		//list.add( "a"@A );
		//Set@A<String> set = new HashSet@A<String>();
		//set.add( "b"@A );
		//Map@A<String, Integer> map = new HashMap@A<String, Integer>();
		//Deque@A<String> deque = new ArrayDeque@A<String>();
		//deque.addFirst( "c"@A );
		//deque.addLast( "d"@A );

		// java.util - utility classes
		Optional@A<String> opt = Optional@A.<String>of( "present"@A );
		boolean@A present = opt.isPresent();
		UUID@A uuid = UUID@A.randomUUID();
		String@A ustr = uuid.toString();
		BitSet@A bits = new BitSet@A();
		bits.set( 0@A );
		//StringJoiner@A sj = new StringJoiner@A( ","@A );
		//sj.add( "x"@A );

		// java.math
		BigInteger@A bi = BigInteger@A.ZERO;
		BigInteger@A bi2 = bi.add( BigInteger@A.ONE );
		BigDecimal@A bd = BigDecimal@A.ONE;
		BigDecimal@A bd2 = bd.multiply( BigDecimal@A.TEN );

		// java.time
		Instant@A now = Instant@A.now();
		LocalDate@A date = LocalDate@A.now();
		LocalTime@A time = LocalTime@A.now();
		LocalDateTime@A dt = LocalDateTime@A.now();
		Duration@A dur = Duration@A.ofSeconds( 60@A );

		// java.io
		ByteArrayOutputStream@A baos = new ByteArrayOutputStream@A();
		StringWriter@A sw = new StringWriter@A();

		// Cross-role usage: B side does independent work
		String@B bs = "world"@B;
		Integer@B bi_b = Integer@B.valueOf( 99@B );
		List@B<String> blist = new ArrayList@B<String>();
		blist.add( bs );
	}
}
