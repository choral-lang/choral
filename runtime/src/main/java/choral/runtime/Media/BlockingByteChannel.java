package choral.runtime.Media;

import java.io.IOException;
import java.nio.channels.ByteChannel;

public interface BlockingByteChannel extends ByteChannel {

	int recvTransmissionLength() throws IOException;

	void sendTransmissionLength( int length ) throws IOException;

}