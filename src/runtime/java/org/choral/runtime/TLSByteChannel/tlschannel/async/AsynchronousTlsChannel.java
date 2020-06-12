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

package org.choral.runtime.TLSByteChannel.tlschannel.async;

import org.choral.runtime.TLSByteChannel.tlschannel.TlsChannel;
import org.choral.runtime.TLSByteChannel.tlschannel.impl.ByteBufferSet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * An {@link AsynchronousByteChannel} that works using {@link TlsChannel}s.
 */
public class AsynchronousTlsChannel implements ExtendedAsynchronousByteChannel {

    private class FutureReadResult extends CompletableFuture<Integer> {
        AsynchronousTlsChannelGroup.ReadOperation op;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            super.cancel(mayInterruptIfRunning);
            return group.doCancelRead(registeredSocket, op);
        }
    }

    private class FutureWriteResult extends CompletableFuture<Integer> {
        AsynchronousTlsChannelGroup.WriteOperation op;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            super.cancel(mayInterruptIfRunning);
            return group.doCancelWrite(registeredSocket, op);
        }
    }

    private final AsynchronousTlsChannelGroup group;
    private final TlsChannel tlsChannel;
    private final AsynchronousTlsChannelGroup.RegisteredSocket registeredSocket;

    /**
     * Initializes a new instance of this class.
     *
     * @param channelGroup  group to associate new new channel to
     * @param tlsChannel    existing TLS channel to be used asynchronously
     * @param socketChannel underlying socket
     * @throws ClosedChannelException   if any of the underlying choral.channels are closed.
     * @throws IllegalArgumentException is the socket is in blocking mode
     */
    public AsynchronousTlsChannel(
            AsynchronousTlsChannelGroup channelGroup,
            TlsChannel tlsChannel,
            SocketChannel socketChannel) throws ClosedChannelException, IllegalArgumentException {
        if (!tlsChannel.isOpen() || !socketChannel.isOpen()) {
            throw new ClosedChannelException();
        }
        if (socketChannel.isBlocking()) {
            throw new IllegalArgumentException("socket channel must be in non-blocking mode");
        }
        this.group = channelGroup;
        this.tlsChannel = tlsChannel;
        this.registeredSocket = channelGroup.registerSocket(tlsChannel, socketChannel);
    }

    @Override
    public <A> void read(
            ByteBuffer dst,
            A attach, CompletionHandler<Integer, ? super A> handler) {
        checkReadOnly(dst);
        if (!dst.hasRemaining()) {
            completeWithZeroInt(attach, handler);
            return;
        }
        group.startRead(
                registeredSocket,
                new ByteBufferSet(dst),
                0, TimeUnit.MILLISECONDS,
                c -> group.executor.submit(() -> handler.completed((int) c, attach)),
                e -> group.executor.submit(() -> handler.failed(e, attach)));
    }

    @Override
    public <A> void read(
            ByteBuffer dst,
            long timeout, TimeUnit unit,
            A attach, CompletionHandler<Integer, ? super A> handler) {
        checkReadOnly(dst);
        if (!dst.hasRemaining()) {
            completeWithZeroInt(attach, handler);
            return;
        }
        group.startRead(
                registeredSocket,
                new ByteBufferSet(dst),
                timeout, unit,
                c -> group.executor.submit(() -> handler.completed((int) c, attach)),
                e -> group.executor.submit(() -> handler.failed(e, attach)));
    }

    @Override
    public <A> void read(
            ByteBuffer[] dsts, int offset, int length,
            long timeout, TimeUnit unit,
            A attach, CompletionHandler<Long, ? super A> handler) {
        ByteBufferSet bufferSet = new ByteBufferSet(dsts, offset, length);
        if (bufferSet.isReadOnly()) {
            throw new IllegalArgumentException("buffer is read-only");
        }
        if (!bufferSet.hasRemaining()) {
            completeWithZeroLong(attach, handler);
            return;
        }
        group.startRead(
                registeredSocket,
                bufferSet,
                timeout, unit,
                c -> group.executor.submit(() -> handler.completed(c, attach)),
                e -> group.executor.submit(() -> handler.failed(e, attach)));
    }

    @Override
    public Future<Integer> read(ByteBuffer dst) {
        checkReadOnly(dst);
        if (!dst.hasRemaining()) {
            return CompletableFuture.completedFuture(0);
        }
        FutureReadResult future = new FutureReadResult();
        AsynchronousTlsChannelGroup.ReadOperation op = group.startRead(
                registeredSocket,
                new ByteBufferSet(dst),
                0, TimeUnit.MILLISECONDS,
                c -> future.complete((int) c),
                future::completeExceptionally);
        future.op = op;
        return future;
    }

    private void checkReadOnly(ByteBuffer dst) {
        if (dst.isReadOnly()) {
            throw new IllegalArgumentException("buffer is read-only");
        }
    }

    @Override
    public <A> void write(ByteBuffer src, A attach, CompletionHandler<Integer, ? super A> handler) {
        if (!src.hasRemaining()) {
            completeWithZeroInt(attach, handler);
            return;
        }
        group.startWrite(
                registeredSocket,
                new ByteBufferSet(src),
                0, TimeUnit.MILLISECONDS,
                c -> group.executor.submit(() -> handler.completed((int) c, attach)),
                e -> group.executor.submit(() -> handler.failed(e, attach)));
    }

    @Override
    public <A> void write(
            ByteBuffer src,
            long timeout, TimeUnit unit,
            A attach, CompletionHandler<Integer, ? super A> handler) {
        if (!src.hasRemaining()) {
            completeWithZeroInt(attach, handler);
            return;
        }
        group.startWrite(
                registeredSocket,
                new ByteBufferSet(src),
                timeout, unit,
                c -> group.executor.submit(() -> handler.completed((int) c, attach)),
                e -> group.executor.submit(() -> handler.failed(e, attach)));
    }

    @Override
    public <A> void write(
            ByteBuffer[] srcs, int offset, int length,
            long timeout, TimeUnit unit,
            A attach, CompletionHandler<Long, ? super A> handler) {
        ByteBufferSet bufferSet = new ByteBufferSet(srcs, offset, length);
        if (!bufferSet.hasRemaining()) {
            completeWithZeroLong(attach, handler);
            return;
        }
        group.startWrite(
                registeredSocket,
                bufferSet,
                timeout, unit,
                c -> group.executor.submit(() -> handler.completed(c, attach)),
                e -> group.executor.submit(() -> handler.failed(e, attach)));
    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
        if (!src.hasRemaining()) {
            return CompletableFuture.completedFuture(0);
        }
        FutureWriteResult future = new FutureWriteResult();
        AsynchronousTlsChannelGroup.WriteOperation op = group.startWrite(
                registeredSocket,
                new ByteBufferSet(src),
                0, TimeUnit.MILLISECONDS,
                c -> future.complete((int) c),
                future::completeExceptionally);
        future.op = op;
        return future;
    }

    private <A> void completeWithZeroInt(A attach, CompletionHandler<Integer, ? super A> handler) {
        group.executor.submit(() -> handler.completed(0, attach));
    }

    private <A> void completeWithZeroLong(A attach, CompletionHandler<Long, ? super A> handler) {
        group.executor.submit(() -> handler.completed(0L, attach));
    }

    /**
     * Tells whether or not this channel is open.
     *
     * @return <tt>true</tt> if, and only if, this channel is open
     */
    @Override
    public boolean isOpen() {
        return tlsChannel.isOpen();
    }

    /**
     * Closes this channel.
     *
     * <p>This method will close the underlying {@link TlsChannel} and also deregister it from its group.</p>
     *
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        tlsChannel.close();
        registeredSocket.close();
    }
}
