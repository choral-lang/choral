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

/**
 * This exception signals the caller that the operation could not continue
 * because a CPU-intensive operation (typically a TLS handshaking) needs to be
 * executed and the {@link TlsChannel} is configured to not run tasks.
 * This allows the application to run these tasks in some other threads, in
 * order to not slow the selection loop. The method that threw the exception
 * should be retried once the task supplied by {@link #getTask()} is executed
 * and finished.
 * <p>
 * This exception is akin to the SSL_ERROR_WANT_ASYNC error code used by OpenSSL
 * (but note that in OpenSSL, the task is executed by the library, while with
 * the {@link TlsChannel}, the calling code is responsible for the
 * execution).
 *
 * @see <a href="https://www.openssl.org/docs/man1.1.0/ssl/SSL_get_error.html">
 *      OpenSSL error documentation</a>
 */
public class NeedsTaskException extends TlsChannelFlowControlException {

	private Runnable task;

	public NeedsTaskException(Runnable task) {
		this.task = task;
	}

	public Runnable getTask() {
		return task;
	}

}
