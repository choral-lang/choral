/*
 * Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 * Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 * Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package choral.compiler;

// import javax.swing.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class SourceWriter {

	public static void writeSource( SourceObject source ) throws IOException {
		writeSource( source, Optional.empty() );
	}

	public static void writeSource( SourceObject source, Path targetFolder ) throws IOException {
		writeSource( source, Optional.ofNullable( targetFolder ), true );
	}

	public static void writeSource(
			SourceObject source, Optional< Path > targetFolder
	) throws IOException {
		writeSource( source, targetFolder, true );
	}

	public static void writeSource(
			SourceObject source, Optional< Path > targetFolder, boolean allowOverwriting
	) throws IOException {
		writeSource( source, targetFolder, true, true );
	}

	public static void writeSource(
			SourceObject source, Optional< Path > targetFolder, boolean useCanonicalPath,
			boolean allowOverwriting
	) throws IOException {
		Path pathToFile = targetFolder
				.map( t -> Paths.get( t.toString(), source.getCanonicalPath() ) )
				.orElseGet( ( useCanonicalPath )
						? () -> Paths.get( source.getCanonicalPath() )
						: () -> Paths.get( source.sourceFile() ) );
		if( pathToFile.getParent() != null ) {
			// files in the cwd may not have a parent if the path is relative.
			Files.createDirectories( pathToFile.getParent() );
		}
		if( allowOverwriting ) {
			Files.write( pathToFile, source.sourceCode().getBytes(), StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING );
		} else {
			Files.write( pathToFile, source.sourceCode().getBytes(),
					StandardOpenOption.CREATE_NEW );
		}
	}

	/*
	public static void writeSources ( Path folder, Map< String, String > units ){
		try {
			createEmptyDirectory( folder );
			units.forEach( ( name, code ) -> writeSources( folder, name, code ) );
		} catch ( CancelException e ) {
			System.out.println( "Compilation cancelled by user" );
		}
	}

	private static void writeSources( Path folder, String name, String code ){
		try {
			FileWriter writer = new FileWriter( folder.resolve( name ).toFile() );
			writer.write( code );
			writer.flush();
			writer.close();
		} catch ( IOException e ) {
			System.err.format( "IOException: %s%n", e );
		}
	}

	private static void createEmptyDirectory( Path path ) throws CancelException {
		int selection = JOptionPane.YES_OPTION;
		if( Files.exists( path ) ) {
			JFrame jf = new JFrame( "Choral Compiler" );
			selection = JOptionPane.showConfirmDialog(
				jf ,
				"WARNING: Folder \"" + path.toAbsolutePath() + "\" is not empty, delete all its content?\n" +
				"Select \"Cancel\" to stop the compilation process; \"No\" continues the compilation overwriting existing files."
				,
				"Choral Compiler",
				JOptionPane.YES_NO_CANCEL_OPTION );
			jf.dispose();
		}
		switch ( selection ){
			case JOptionPane.YES_OPTION :
				try {
					if( Files.exists( path ) ){
						Files.walk( path )
							.map( Path::toFile )
							.forEach( File::delete );
						Files.deleteIfExists( path );
					}
					Files.createDirectory( path );
				} catch ( IOException e ) {
					e.printStackTrace();
				}
				break;
			case JOptionPane.CANCEL_OPTION :
				throw new CancelException();
		}
	}

	private static class CancelException extends Exception {}
	*/

}
