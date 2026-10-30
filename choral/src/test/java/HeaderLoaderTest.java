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

	@Test
	public void ignoresHeaderWhenSourcePathUsesAnAlias( @TempDir Path project )
			throws Exception {
		Files.writeString( project.resolve( "Example.ch" ), "class Example@( A ) {}" );
		Files.writeString( project.resolve( "Example.chh" ), "class Example@( A ) {}" );
		Path sourceAlias = project.resolve( "." ).resolve( "Example.ch" );

		var headers = HeaderLoader.loadFromPath(
				List.of( project ), List.of( sourceAlias.toFile() ), true, true ).toList();

		assertTrue( headers.isEmpty() );
	}
}
