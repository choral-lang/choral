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

import choral.ast.Node;

import java.util.EnumSet;

public class Logger {

	private String file = "";
	private boolean errors = false;

	public String file() {
		return file;
	}

	public void setFile( String file ) {
		this.file = ( file == null ) ? "" : file;
	}

	public static String getFormattedPosition( String file, int line, int column ) {
		if( file == null || file.isBlank() ) {
			return String.format( "line %d column %d", line, column );
		} else {
			return String.format( "'%s' line %d column %d", file, line, column );
		}
	}

	public static String addFormattedPosition( String position, String message ) {
		return message + "\n\t see " + position;
	}

/*    public static String getFormattedPosition(String file, Node node){
        return getFormattedPosition(file, node.line(), node.column());
    }

    public String getFormattedPosition(int line, int column){
        return getFormattedPosition(this.file(), line, column);
    }

    public String getFormattedPosition(Node node){
        return getFormattedPosition(this.file(), node.line(), node.column());
    }*/

	public final EnumSet< Level > filterLevels = EnumSet.of( Level.ERROR, Level.WARNING );

	public enum Level {
		ERROR( -1 ),
		WARNING( 0 ),
		INFO( 1 ),
		DEBUG( 2 );

		final int value;

		Level( int value ) {
			this.value = value;
		}
	}

	public Logger( Logger logger ) {
		this.filterLevels.clear();
		for( Level l : logger.filterLevels ) {
			this.filterLevels.add( l );
		}
		setFile( logger.file );
	}

	public Logger( Logger logger, String file ) {
		this( logger );
		setFile( file );
	}

	public Logger( Level... filterLevels ) {
		this.filterLevels.clear();
		for( Level l : filterLevels ) {
			this.filterLevels.add( l );
		}
	}

	public boolean hasErrors() {
		return errors;
	}

	public void flagErrors() {
		errors = true;
	}

	public void logf( Level level, String format, Object... args ) {
		log( level, String.format( format, args ) );
	}

	public void logfWithPosition(
			Level level, int line, int column, String format, Object... args
	) {
		logfWithPosition( level, file(), line, column, format, args );
	}

	public void logfWithPosition(
			Level level, String file, int line, int column, String format, Object... args
	) {
		log( level, addFormattedPosition( getFormattedPosition( file, line, column ),
				String.format( format, args ) ) );
	}

	public void log( Level level, String message ) {
		if( filterLevels.contains( level ) ) {
			switch( level ) {
				case ERROR:
					this.errors = true;
					System.err.println( "[ERROR] " + message );
					break;
				case WARNING:
					System.out.println( "[WARNING] " + message );
					break;
				case INFO:
					System.out.println( "[INFO] " + message );
					break;
				case DEBUG:
					System.out.println( "[DEBUG] " + message );
					break;
			}
		}
	}
}
