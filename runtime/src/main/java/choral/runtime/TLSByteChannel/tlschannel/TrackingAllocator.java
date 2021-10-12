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

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * A decorating {@link BufferAllocator} that keeps statistics.
 */
public class TrackingAllocator implements BufferAllocator {

    private BufferAllocator impl;

    private LongAdder bytesAllocatedAdder = new LongAdder();
    private LongAdder bytesDeallocatedAdder = new LongAdder();
    private AtomicLong currentAllocationSize = new AtomicLong();
    private LongAccumulator maxAllocationSizeAcc = new LongAccumulator(Math::max, 0);

    private LongAdder buffersAllocatedAdder = new LongAdder();
    private LongAdder buffersDeallocatedAdder = new LongAdder();

    public TrackingAllocator(BufferAllocator impl) {
        this.impl = impl;
    }

    public ByteBuffer allocate(int size) {
        bytesAllocatedAdder.add(size);
        currentAllocationSize.addAndGet(size);
        buffersAllocatedAdder.increment();
        return impl.allocate(size);
    }

    public void free(ByteBuffer buffer) {
        int size = buffer.capacity();
        bytesDeallocatedAdder.add(size);
        maxAllocationSizeAcc.accumulate(currentAllocationSize.longValue());
        currentAllocationSize.addAndGet(-size);
        buffersDeallocatedAdder.increment();
        impl.free(buffer);
    }

    public long bytesAllocated() {
        return bytesAllocatedAdder.longValue();
    }

    public long bytesDeallocated() {
        return bytesDeallocatedAdder.longValue();
    }

    public long currentAllocation() {
        return currentAllocationSize.longValue();
    }

    public long maxAllocation() {
        return maxAllocationSizeAcc.longValue();
    }

    public long buffersAllocated() {
        return buffersAllocatedAdder.longValue();
    }

    public long buffersDeallocated() {
        return buffersDeallocatedAdder.longValue();
    }
}
