import choral.compiler.HeaderLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HeaderLoaderTest {
	@Test
	public void ignoresHeaderWhenMatchingSourceIsPresent( @TempDir Path project )
			throws Exception {
		Path source = project.resolve( "Example.ch" );
		Files.writeString( source, "class Example@( A ) {}" );
		Files.writeString( project.resolve( "Example.chh" ), "class Example@( A ) {}" );

		var headers = HeaderLoader.loadFromPath(
				List.of( project ), List.of( source.toFile() ), true, true ).toList();

		assertTrue( headers.isEmpty() );
	}
}
