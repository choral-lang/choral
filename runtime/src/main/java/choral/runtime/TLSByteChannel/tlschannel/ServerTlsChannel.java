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

import choral.runtime.TLSByteChannel.tlschannel.impl.BufferHolder;
import choral.runtime.TLSByteChannel.tlschannel.impl.ByteBufferSet;
import choral.runtime.TLSByteChannel.tlschannel.impl.TlsChannelImpl;
import choral.runtime.TLSByteChannel.tlschannel.impl.TlsExplorer;
import choral.runtime.TLSByteChannel.tlschannel.util.TlsChannelCallbackException;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A server-side {@link TlsChannel}.
 */
public class ServerTlsChannel implements TlsChannel {

	private interface SslContextStrategy {

		@FunctionalInterface
		interface SniReader {
			Optional< SNIServerName > readSni() throws IOException, TlsChannelImpl.EofException;
		}

		SSLContext getSslContext(
				SniReader sniReader
		) throws IOException, TlsChannelImpl.EofException;

	}

	private static class SniSslContextStrategy implements SslContextStrategy {

		private SniSslContextFactory sniSslContextFactory;

		public SniSslContextStrategy( SniSslContextFactory sniSslContextFactory ) {
			this.sniSslContextFactory = sniSslContextFactory;
		}

		@Override
		public SSLContext getSslContext(
				SniReader sniReader
		) throws IOException, TlsChannelImpl.EofException {
			// IO block
			Optional< SNIServerName > nameOpt = sniReader.readSni();
			// call client code
			Optional< SSLContext > chosenContext;
			try {
				chosenContext = sniSslContextFactory.getSslContext( nameOpt );
			} catch( Exception e ) {
//                logger.trace("client code threw exception during evaluation of server name indication", e);
				throw new TlsChannelCallbackException( "SNI callback failed", e );
			}
			return chosenContext.orElseThrow(
					() -> new SSLHandshakeException(
							"No ssl context available for received SNI: " + nameOpt ) );
		}

	}

	private static class FixedSslContextStrategy implements SslContextStrategy {

		private final SSLContext sslContext;

		public FixedSslContextStrategy( SSLContext sslContext ) {
			this.sslContext = sslContext;
		}

		@Override
		public SSLContext getSslContext( SniReader sniReader ) {
			/*
			 * Avoid SNI parsing (using the supplied sniReader) when no decision
			 * would be made based on it.
			 */
			return sslContext;
		}

	}

	private static SSLEngine defaultSSLEngineFactory( SSLContext sslContext ) {
		SSLEngine engine = sslContext.createSSLEngine();
		engine.setUseClientMode( false );
		return engine;
	}

	/**
	 * Builder of {@link ServerTlsChannel}
	 */
	public static class Builder extends TlsChannelBuilder< Builder > {

		private final SslContextStrategy internalSslContextFactory;
		private Function< SSLContext, SSLEngine > sslEngineFactory = ServerTlsChannel::defaultSSLEngineFactory;

		private Builder( ByteChannel underlying, SSLContext sslContext ) {
			super( underlying );
			this.internalSslContextFactory = new FixedSslContextStrategy( sslContext );
		}

		private Builder( ByteChannel wrapped, SniSslContextFactory sslContextFactory ) {
			super( wrapped );
			this.internalSslContextFactory = new SniSslContextStrategy( sslContextFactory );
		}

		@Override
		Builder getThis() {
			return this;
		}

		public Builder withEngineFactory( Function< SSLContext, SSLEngine > sslEngineFactory ) {
			this.sslEngineFactory = sslEngineFactory;
			return this;
		}

		public ServerTlsChannel build() {
			return new ServerTlsChannel( underlying, internalSslContextFactory, sslEngineFactory,
					sessionInitCallback,
					runTasks, plainBufferAllocator, encryptedBufferAllocator, releaseBuffers,
					waitForCloseConfirmation );
		}

	}

	/**
	 * Create a new {@link Builder}, configured with a underlying
	 * {@link Channel} and a fixed {@link SSLContext}, which will be used to
	 * create the {@link SSLEngine}.
	 *
	 * @param underlying a reference to the underlying {@link ByteChannel}
	 * @param sslContext a fixed {@link SSLContext} to be used
	 */
	public static Builder newBuilder( ByteChannel underlying, SSLContext sslContext ) {
		return new Builder( underlying, sslContext );
	}

	/**
	 * <p> Create a new {@link Builder}, configured with a underlying {@link Channel} and a custom {@link SSLContext}
	 * factory, which will be used to create the context (in turn used to create the {@link SSLEngine}, as a function of
	 * the SNI received at the TLS connection start. </p>
	 *
	 * <p><b>Implementation note:</b><br> Due to limitations of {@link SSLEngine}, configuring a {@link
	 * ServerTlsChannel} to select the {@link SSLContext} based on the SNI value implies parsing the first TLS frame
	 * (ClientHello) independently of the SSLEngine. </p>
	 *
	 * @param underlying        a reference to the underlying {@link ByteChannel}
	 * @param sslContextFactory a function from an optional SNI to the {@link SSLContext} to be used
	 * @see <a href="https://tools.ietf.org/html/rfc6066#section-3">Server Name Indication</a>
	 */
	public static Builder newBuilder(
			ByteChannel underlying, SniSslContextFactory sslContextFactory
	) {
		return new Builder( underlying, sslContextFactory );
	}

	private final ByteChannel underlying;
	private final SslContextStrategy sslContextStrategy;
	private final Function< SSLContext, SSLEngine > engineFactory;
	private final Consumer< SSLSession > sessionInitCallback;
	private final boolean runTasks;
	private final TrackingAllocator plainBufAllocator;
	private final TrackingAllocator encryptedBufAllocator;
	private final boolean releaseBuffers;
	private final boolean waitForCloseConfirmation;

	private final Lock initLock = new ReentrantLock();

	private BufferHolder inEncrypted;

	private volatile boolean sniRead = false;
	private SSLContext sslContext = null;
	private TlsChannelImpl impl = null;

	// @formatter:off
	private ServerTlsChannel(
			ByteChannel underlying,
			SslContextStrategy internalSslContextFactory,
			Function< SSLContext, SSLEngine > engineFactory,
			Consumer< SSLSession > sessionInitCallback,
			boolean runTasks,
			BufferAllocator plainBufAllocator,
			BufferAllocator encryptedBufAllocator,
			boolean releaseBuffers,
			boolean waitForCloseConfirmation
	) {
		this.underlying = underlying;
		this.sslContextStrategy = internalSslContextFactory;
		this.engineFactory = engineFactory;
		this.sessionInitCallback = sessionInitCallback;
		this.runTasks = runTasks;
		this.plainBufAllocator = new TrackingAllocator( plainBufAllocator );
		this.encryptedBufAllocator = new TrackingAllocator( encryptedBufAllocator );
		this.releaseBuffers = releaseBuffers;
		this.waitForCloseConfirmation = waitForCloseConfirmation;
		inEncrypted = new BufferHolder(
				"inEncrypted",
				Optional.empty(),
				encryptedBufAllocator,
				TlsChannelImpl.buffersInitialSize,
				TlsChannelImpl.maxTlsPacketSize,
				false /* plainData */,
				releaseBuffers );
	}

	// @formatter:on

	@Override
	public ByteChannel getUnderlying() {
		return underlying;
	}

	/**
	 * Return the used {@link SSLContext}.
	 *
	 * @return if context if present, of null if the TLS connection as not been
	 * initializer, or the SNI not received yet.
	 */
	public SSLContext getSslContext() {
		return sslContext;
	}

	@Override
	public SSLEngine getSslEngine() {
		return impl == null ? null : impl.engine();
	}

	@Override
	public Consumer< SSLSession > getSessionInitCallback() {
		return sessionInitCallback;
	}

	@Override
	public boolean getRunTasks() {
		return impl.getRunTasks();
	}

	@Override
	public TrackingAllocator getPlainBufferAllocator() {
		return plainBufAllocator;
	}

	@Override
	public TrackingAllocator getEncryptedBufferAllocator() {
		return encryptedBufAllocator;
	}

	@Override
	public long read( ByteBuffer[] dstBuffers, int offset, int length ) throws IOException {
		ByteBufferSet dest = new ByteBufferSet( dstBuffers, offset, length );
		TlsChannelImpl.checkReadBuffer( dest );
		if( !sniRead ) {
			try {
				initEngine();
			} catch( TlsChannelImpl.EofException e ) {
				return -1;
			}
		}
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
	public long write( ByteBuffer[] srcs, int offset, int length ) throws IOException {
		ByteBufferSet source = new ByteBufferSet( srcs, offset, length );
		if( !sniRead ) {
			try {
				initEngine();
			} catch( TlsChannelImpl.EofException e ) {
				throw new ClosedChannelException();
			}
		}
		return impl.write( source );
	}

	@Override
	public long write( ByteBuffer[] srcs ) throws IOException {
		return write( srcs, 0, srcs.length );
	}

	@Override
	public int write( ByteBuffer srcBuffer ) throws IOException {
		return (int) write( new ByteBuffer[] { srcBuffer } );
	}

	@Override
	public void renegotiate() throws IOException {
		if( !sniRead ) {
			try {
				initEngine();
			} catch( TlsChannelImpl.EofException e ) {
				throw new ClosedChannelException();
			}
		}
		impl.renegotiate();
	}

	@Override
	public void handshake() throws IOException {
		if( !sniRead ) {
			try {
				initEngine();
			} catch( TlsChannelImpl.EofException e ) {
				throw new ClosedChannelException();
			}
		}
		impl.handshake();
	}

	@Override
	public void close() throws IOException {
		if( impl != null )
			impl.close();
		if( inEncrypted != null )
			inEncrypted.dispose();
		underlying.close();
	}

	@Override
	public boolean isOpen() {
		return underlying.isOpen();
	}

	private void initEngine() throws IOException, TlsChannelImpl.EofException {
		initLock.lock();
		try {
			if( !sniRead ) {
				sslContext = sslContextStrategy.getSslContext( this::getServerNameIndication );
				// call client code
				SSLEngine engine;
				try {
					engine = engineFactory.apply( sslContext );
				} catch( Exception e ) {
//                    logger.trace("client threw exception in SSLEngine factory", e);
					throw new TlsChannelCallbackException( "SSLEngine creation callback failed",
							e );
				}
				impl = new TlsChannelImpl( underlying, underlying, engine,
						Optional.of( inEncrypted ), sessionInitCallback,
						runTasks, plainBufAllocator, encryptedBufAllocator, releaseBuffers,
						waitForCloseConfirmation );
				inEncrypted = null;
				sniRead = true;
			}
		} finally {
			initLock.unlock();
		}
	}

	private Optional< SNIServerName > getServerNameIndication() throws IOException, TlsChannelImpl.EofException {
		inEncrypted.prepare();
		try {
			int recordHeaderSize = readRecordHeaderSize();
			while( inEncrypted.buffer.position() < recordHeaderSize ) {
				if( !inEncrypted.buffer.hasRemaining() ) {
					inEncrypted.enlarge();
				}
				TlsChannelImpl.readFromChannel( underlying, inEncrypted.buffer ); // IO block
			}
			inEncrypted.buffer.flip();
			Map< Integer, SNIServerName > serverNames = TlsExplorer.explore( inEncrypted.buffer );
			inEncrypted.buffer.compact();
			SNIServerName hostName = serverNames.get( StandardConstants.SNI_HOST_NAME );
			if( hostName != null && hostName instanceof SNIHostName ) {
				SNIHostName sniHostName = (SNIHostName) hostName;
				return Optional.of( sniHostName );
			} else {
				return Optional.empty();
			}
		} finally {
			inEncrypted.release();
		}
	}

	private int readRecordHeaderSize() throws IOException, TlsChannelImpl.EofException {
		while( inEncrypted.buffer.position() < TlsExplorer.RECORD_HEADER_SIZE ) {
			if( !inEncrypted.buffer.hasRemaining() ) {
				throw new IllegalStateException( "inEncrypted too small" );
			}
			TlsChannelImpl.readFromChannel( underlying, inEncrypted.buffer ); // IO block
		}
		inEncrypted.buffer.flip();
		int recordHeaderSize = TlsExplorer.getRequiredSize( inEncrypted.buffer );
		inEncrypted.buffer.compact();
		return recordHeaderSize;
	}

	@Override
	public boolean shutdown() throws IOException {
		return impl != null && impl.shutdown();
	}

	@Override
	public boolean shutdownReceived() {
		return impl != null && impl.shutdownReceived();
	}

	@Override
	public boolean shutdownSent() {
		return impl != null && impl.shutdownSent();
	}

}
