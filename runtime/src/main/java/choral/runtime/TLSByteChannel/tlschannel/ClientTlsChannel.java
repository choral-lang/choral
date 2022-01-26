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

import choral.runtime.TLSByteChannel.tlschannel.impl.ByteBufferSet;
import choral.runtime.TLSByteChannel.tlschannel.impl.TlsChannelImpl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A client-side {@link TlsChannel}.
 */
public class ClientTlsChannel implements TlsChannel {

	/**
	 * Builder of {@link ClientTlsChannel}
	 */
	public static class Builder extends TlsChannelBuilder< Builder > {

		private Supplier< SSLEngine > sslEngineFactory;

		private Builder( ByteChannel underlying, SSLEngine sslEngine ) {
			super( underlying );
			this.sslEngineFactory = () -> sslEngine;
		}

		private Builder( ByteChannel underlying, SSLContext sslContext ) {
			super( underlying );
			this.sslEngineFactory = () -> defaultSSLEngineFactory( sslContext );
		}

		@Override
		Builder getThis() {
			return this;
		}

		public ClientTlsChannel build() {
			return new ClientTlsChannel( underlying, sslEngineFactory.get(), sessionInitCallback,
					runTasks,
					plainBufferAllocator, encryptedBufferAllocator, releaseBuffers,
					waitForCloseConfirmation );
		}

	}

	private static SSLEngine defaultSSLEngineFactory( SSLContext sslContext ) {
		SSLEngine engine = sslContext.createSSLEngine();
		engine.setUseClientMode( true );
		return engine;
	}

	/**
	 * Create a new {@link Builder}, configured with a underlying
	 * {@link Channel} and a fixed {@link SSLEngine}.
	 *
	 * @param underlying a reference to the underlying {@link ByteChannel}
	 * @param sslEngine  the engine to use with this channel
	 */
	public static Builder newBuilder( ByteChannel underlying, SSLEngine sslEngine ) {
		return new Builder( underlying, sslEngine );
	}

	/**
	 * Create a new {@link Builder}, configured with a underlying
	 * {@link Channel} and a {@link SSLContext}.
	 *
	 * @param underlying a reference to the underlying {@link ByteChannel}
	 * @param sslContext a context to use with this channel, it will be used to create a client {@link SSLEngine}.
	 */
	public static Builder newBuilder( ByteChannel underlying, SSLContext sslContext ) {
		return new Builder( underlying, sslContext );
	}

	private final ByteChannel underlying;
	private final TlsChannelImpl impl;

	private ClientTlsChannel(
			ByteChannel underlying,
			SSLEngine engine,
			Consumer< SSLSession > sessionInitCallback,
			boolean runTasks,
			BufferAllocator plainBufAllocator,
			BufferAllocator encryptedBufAllocator,
			boolean releaseBuffers,
			boolean waitForCloseNotifyOnClose
	) {
		if( !engine.getUseClientMode() )
			throw new IllegalArgumentException( "SSLEngine must be in client mode" );
		this.underlying = underlying;
		TrackingAllocator trackingPlainBufAllocator = new TrackingAllocator( plainBufAllocator );
		TrackingAllocator trackingEncryptedAllocator = new TrackingAllocator(
				encryptedBufAllocator );
		impl = new TlsChannelImpl( underlying, underlying, engine, Optional.empty(),
				sessionInitCallback, runTasks,
				trackingPlainBufAllocator, trackingEncryptedAllocator, releaseBuffers,
				waitForCloseNotifyOnClose );
	}

	@Override
	public ByteChannel getUnderlying() {
		return underlying;
	}

	@Override
	public SSLEngine getSslEngine() {
		return impl.engine();
	}

	@Override
	public Consumer< SSLSession > getSessionInitCallback() {
		return impl.getSessionInitCallback();
	}

	@Override
	public TrackingAllocator getPlainBufferAllocator() {
		return impl.getPlainBufferAllocator();
	}

	@Override
	public TrackingAllocator getEncryptedBufferAllocator() {
		return impl.getEncryptedBufferAllocator();
	}

	@Override
	public boolean getRunTasks() {
		return impl.getRunTasks();
	}

	@Override
	public long read( ByteBuffer[] dstBuffers, int offset, int length ) throws IOException {
		ByteBufferSet dest = new ByteBufferSet( dstBuffers, offset, length );
		TlsChannelImpl.checkReadBuffer( dest );
		return impl.read( dest );
	}

	@Override
	public long read( ByteBuffer[] dstBuffers ) throws IOException {
		return read( dstBuffers, 0, dstBuffers.length );
	}

	@Override
	public int read( ByteBuffer dstBuffer ) throws IOException {
		return (int) read( new ByteBuffer[] { dstBuffer } );
	}

	@Override
	public long write( ByteBuffer[] srcBuffers, int offset, int length ) throws IOException {
		ByteBufferSet source = new ByteBufferSet( srcBuffers, offset, length );
		return impl.write( source );
	}

	@Override
	public long write( ByteBuffer[] outs ) throws IOException {
		return write( outs, 0, outs.length );
	}

	@Override
	public int write( ByteBuffer srcBuffer ) throws IOException {
		return (int) write( new ByteBuffer[] { srcBuffer } );
	}

	@Override
	public void renegotiate() throws IOException {
		impl.renegotiate();
	}

	@Override
	public void handshake() throws IOException {
		impl.handshake();
	}

	@Override
	public void close() throws IOException {
		impl.close();
	}

	@Override
	public boolean isOpen() {
		return impl.isOpen();
	}

	@Override
	public boolean shutdown() throws IOException {
		return impl.shutdown();
	}

	@Override
	public boolean shutdownReceived() {
		return impl.shutdownReceived();
	}

	@Override
	public boolean shutdownSent() {
		return impl.shutdownSent();
	}

}
