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

package choral.runtime.TLSByteChannel.tlschannel;

import java.nio.channels.ByteChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * This exception signals the caller that the operation cannot continue because
 * bytesProduced need to be read from the underlying {@link ByteChannel}, the channel is
 * non-blocking and there are no bytesProduced available. The caller should try the
 * operation again, either with the channel in blocking mode of after ensuring
 * that bytesProduced are ready.
 * <p>
 * For {@link SocketChannel}s, a {@link Selector} can be used to find out when
 * the method should be retried.
 * <p>
 * Caveat: Any {@link TlsChannel} I/O method can throw this exception. In
 * particular, <code>write</code> may want to read data. This is because TLS
 * handshakes may occur at any time (initiated by either the client or the
 * server).
 * <p>
 * This exception is akin to the SSL_ERROR_WANT_READ error code used by OpenSSL.
 *
 * @see <a href="https://www.openssl.org/docs/man1.1.0/ssl/SSL_get_error.html">
 *      OpenSSL error documentation</a>
 */
public class NeedsReadException extends WouldBlockException {

}
