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

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.util.function.Consumer;

/**
 * Base class for builders of {@link TlsChannel}.
 */
public abstract class TlsChannelBuilder< T extends TlsChannelBuilder< T > > {

	final ByteChannel underlying;

	// @formatter:off
	Consumer< SSLSession > sessionInitCallback = session -> {
	};
	// @formatter:on
	boolean runTasks = true;
	BufferAllocator plainBufferAllocator = TlsChannel.defaultPlainBufferAllocator;
	BufferAllocator encryptedBufferAllocator = TlsChannel.defaultEncryptedBufferAllocator;
	boolean releaseBuffers = true;
	boolean waitForCloseConfirmation = false;

	TlsChannelBuilder( ByteChannel underlying ) {
		this.underlying = underlying;
	}

	abstract T getThis();

	/**
	 * Whether CPU-intensive tasks are run or not. Default is to do run them. If
	 * setting this <code>false</code>, the calling code should be prepared to handle
	 * {@link NeedsTaskException}}
	 */
	public T withRunTasks( boolean runTasks ) {
		this.runTasks = runTasks;
		return getThis();
	}

	/**
	 * Set the {@link BufferAllocator} to use for unencrypted data. By default a
	 * {@link HeapBufferAllocator} is used, as this buffers are used to
	 * supplement user-supplied ones when dealing with too big a TLS record,
	 * that is, they operate entirely inside the JVM.
	 */
	public T withPlainBufferAllocator( BufferAllocator bufferAllocator ) {
		this.plainBufferAllocator = bufferAllocator;
		return getThis();
	}

	/**
	 * Set the {@link BufferAllocator} to use for encrypted data. By default a
	 * {@link DirectBufferAllocator} is used, as this data is usually read from or
	 * written to native sockets.
	 */
	public T withEncryptedBufferAllocator( BufferAllocator bufferAllocator ) {
		this.encryptedBufferAllocator = bufferAllocator;
		return getThis();
	}

	/**
	 * Register a callback function to be executed when the TLS session is
	 * established (or re-established). The supplied function will run in the
	 * same thread as the rest of the handshake, so it should ideally run as
	 * fast as possible.
	 */
	public T withSessionInitCallback( Consumer< SSLSession > sessionInitCallback ) {
		this.sessionInitCallback = sessionInitCallback;
		return getThis();
	}

	/**
	 * Whether to release unused buffers in the mid of connections. Equivalent to
	 * OpenSSL's SSL_MODE_RELEASE_BUFFERS.
	 * <p>
	 * Default is to release. Releasing unused buffers is specially effective
	 * in the case case of idle long-lived connections, when the memory footprint
	 * can be reduced significantly. A potential reason for setting this value
	 * to <code>false</code> is performance, since more releases means more
	 * allocations, which have a cost. This is effectively a memory-time trade-off.
	 * However, in most cases the default behavior makes sense.
	 */
	public T withReleaseBuffers( boolean releaseBuffers ) {
		this.releaseBuffers = releaseBuffers;
		return getThis();
	}

	/**
	 * <p> Whether to wait for TLS close confirmation when executing a local {@link TlsChannel#close()} on the channel.
	 * If the underlying channel is blocking, setting this to <code>true</code> will block (potentially until it times
	 * out, or indefinitely) the close operation until the counterpart confirms the close on their side (sending a
	 * close_notify alert. If the underlying channel is non-blocking, setting this parameter to true is ineffective.
	 * </p>
	 *
	 * <p> Setting this value to <code>true</code> emulates the behavior of {@link SSLSocket} when used in layered mode
	 * (and without autoClose). </p>
	 *
	 * <p> Even when this behavior is enabled, the close operation will not propagate any {@link IOException} thrown
	 * during the TLS close exchange and just proceed to close the underlying channel. </p>
	 *
	 * <p> Default is to not wait and close immediately. The proper closing procedure can be initiated at any moment
	 * using {@link TlsChannel#shutdown()}.</p>
	 *
	 * @see TlsChannel#shutdown()
	 */
	public T withWaitForCloseConfirmation( boolean waitForCloseConfirmation ) {
		this.waitForCloseConfirmation = waitForCloseConfirmation;
		return getThis();
	}

}
