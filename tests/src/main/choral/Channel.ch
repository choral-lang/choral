package a.b.c;

public interface Channel@( Sender, Receiver )< T@( W ) > {
	< S@(W) extends T@(W) > S@( Receiver ) com( S@( Sender ) msg );
}

final class JSONChannel@( Sender, Receiver ) implements Channel@( Sender, Receiver )< JSONSerializable@( W ) > {
	private MessageQueue@( Sender ) messageQueueSender;
	private MessageQueue@( Receiver ) messageQueueReceiver;

	public JSONChannel( MessageQueue@( Sender ) messageQueueSender, MessageQueue@( Receiver ) messageQueueReceiver ){
		this.messageQueueSender = messageQueueSender;
		this.messageQueueReceiver = messageQueueReceiver;
	}

	public final < S@( W ) extends JSONSerializable@( W ) > S@( Receiver ) com( S@( Sender ) msg ){
		messageQueueSender.send( msg );
		S@( Receiver ) message;
		try {
			message = messageQueue.recv();
		} catch ( ExecutionException@( Receiver ) e ) {
			e.printStackTrace();
		} catch ( InterruptedException@( Receiver ) e ) {
			e.printStackTrace();
		}
		return message;
	}

}
