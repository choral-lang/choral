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

package org.choral.compiler;

import com.google.common.collect.Streams;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.choral.Choral;
import org.choral.ast.CompilationUnit;

import java.io.*;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.choral.utils.Streams.*;

public class HeaderLoader {

	public static void test() throws IOException {
		loadStandardProfile();
	}

	public static Stream< CompilationUnit > loadProfile( String profile ) throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		List< String > files = new LinkedList<>();
		try(
				InputStream in = cl.getResourceAsStream( "headers/standard.profile" );
				BufferedReader br = new BufferedReader( new InputStreamReader( in ) ) ) {
			String file;
			while( ( file = br.readLine() ) != null ) {
				files.add( file );
			}
		}
		List< CompilationUnit > headers = new ArrayList<>( files.size() );
		for( String file : files ) {
			try( InputStream in = cl.getResourceAsStream( "headers/" + file ) ) {
				headers.add( Parser.parseSourceFile( cl.getResourceAsStream( "headers/" + file ),
						"choral://" + file ) );
			}
		}
		return headers.stream();
	}

	public static Stream< CompilationUnit > loadStandardProfile() throws IOException {
		return loadProfile( "standard" );
	}

	public static Stream< CompilationUnit > loadFromPath(
			Collection< Path > folders
	) throws IOException {
		return loadFromPath( folders, true );
	}

	public static Stream< CompilationUnit > loadFromPath(
			Collection< Path > folders, boolean ignoreIfSourcePresent
	) throws IOException {
		List< File > files = folders.stream().flatMap( wrapFunction( p -> Files.find( p, 999,
				( q, a ) -> !a.isDirectory() && keepHeaderFile( q, ignoreIfSourcePresent )
				, FileVisitOption.FOLLOW_LINKS ) ) )
				.map( Path::toFile )
				.collect( Collectors.toList() );
		ArrayList< CompilationUnit > headers = new ArrayList<>( files.size() );
		for( File file : files ) {
			headers.add( Parser.parseSourceFile( file ) );
		}
		return headers.stream();
	}

	private static boolean keepHeaderFile( Path file, boolean ignoreIfSourcePresent ) {
		String f = file.toString();
		if( f.toLowerCase().endsWith( SourceObject.HeaderSourceObject.FILE_EXTENSION ) ) {
			if( ignoreIfSourcePresent ) {
				String s = f.substring(
						f.length() - SourceObject.HeaderSourceObject.FILE_EXTENSION.length() ) + SourceObject.ChoralSourceObject.FILE_EXTENSION;
				return !Files.exists( Paths.get( s ) );
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

}
