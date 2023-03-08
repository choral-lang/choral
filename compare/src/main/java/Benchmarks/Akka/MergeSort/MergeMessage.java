package Benchmarks.Akka.MergeSort;

import java.util.List;

public class MergeMessage implements Message {

	static class MergeLeftMessage extends MergeMessage {

		public MergeLeftMessage( List< Integer > list ) {
			super( list );
		}
	}

	static class MergeRightMessage extends MergeMessage {

		public MergeRightMessage( List< Integer > list ) {
			super( list );
		}
	}

	private final List< Integer > list;

	public MergeMessage( List< Integer > list ) {
		this.list = list;
	}

	@Override
	public List< Integer > list() {
		return list;
	}

}
