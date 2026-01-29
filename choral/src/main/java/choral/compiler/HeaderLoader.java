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

import choral.ast.CompilationUnit;

import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static choral.utils.Streams.wrapFunction;

public class HeaderLoader {

	public static void test() throws IOException {
		loadStandardProfile();
	}

	public static Stream< CompilationUnit > loadProfile(
			String profile
	) throws IOException {
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
				headers.add( Parser.parseSourceFile( in,
						"headers/" + file ) );
			}
		}
		return headers.stream();
	}

	public static Stream< CompilationUnit > loadProfileForClassLifter(
			String profile
	) throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		List< String > files = new LinkedList<>();
		try(
				InputStream in = cl.getResourceAsStream( "headers/alternate.profile" );
				BufferedReader br = new BufferedReader( new InputStreamReader( in ) ) ) {
			String file;
			while( ( file = br.readLine() ) != null ) {
				files.add( file );
			}
		}
		List< CompilationUnit > headers = new ArrayList<>( files.size() );
		for( String file : files ) {
			try( InputStream in = cl.getResourceAsStream( "headers/" + file ) ) {
				headers.add( Parser.parseSourceFile( in,
						"headers/" + file ) );
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
		return loadFromPath( folders, List.of(), ignoreIfSourcePresent, true );
	}

	public static Stream< CompilationUnit > loadFromPath(
			Collection< Path > headersPaths, Collection< File > sourceFiles,
			boolean ignoreIfSourcePresent, boolean strictHeaderSearch
	) throws IOException {
		Stream< Path > pathsFromHeaders = headersPaths.stream().flatMap(
				wrapFunction( p -> Files.find( p, 999,
						( q, a ) -> !a.isDirectory() && keepHeaderFile( q, sourceFiles,
								ignoreIfSourcePresent )
						, FileVisitOption.FOLLOW_LINKS ) ) );
		Stream< Path > pathsFromSources;
		if( strictHeaderSearch ) {
			pathsFromSources = Stream.of();
		} else {
			pathsFromSources = sourceFiles.stream().map(
							x -> Paths.get( ( x.isDirectory() )
									? x.getPath()
									: ( x.getParent() == null ? "" : x.getParent() ) ) )
					.flatMap( wrapFunction( p -> Files.find( p, 1,
							( q, a ) -> !a.isDirectory() && keepHeaderFile( q, sourceFiles,
									ignoreIfSourcePresent )
							, FileVisitOption.FOLLOW_LINKS ) ) );
		}
		List< File > files = Stream.concat( pathsFromHeaders, pathsFromSources )
				.map( Path::toFile )
				.distinct()
				.collect( Collectors.toList() );
		ArrayList< CompilationUnit > headers = new ArrayList<>( files.size() );
		for( File file : files ) {
			headers.add( Parser.parseSourceFile( file ) );
		}
		return headers.stream();
	}

	private static boolean keepHeaderFile(
			Path file, Collection< File > sourceFiles, boolean ignoreIfSourcePresent
	) {
		String f = file.toString();
		if( f.toLowerCase().endsWith( SourceObject.HeaderSourceObject.FILE_EXTENSION ) ) {
			if( ignoreIfSourcePresent ) {
				String s = f.substring(
						f.length() - SourceObject.HeaderSourceObject.FILE_EXTENSION.length() )
						+ SourceObject.ChoralSourceObject.FILE_EXTENSION;
				for( File sf : sourceFiles ) {
					if( Paths.get( s ).compareTo( sf.toPath() ) == 0 ) {
						return false;
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}
}
