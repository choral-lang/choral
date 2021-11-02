package MergeSort;

import akka.actor.typed.ActorRef;

import java.util.List;

public class SortMessage implements Message {

	private final List< Integer > list;
	private final ActorRef< Message > merger;

	public SortMessage(
			List< Integer > list, ActorRef< Message > merger
	) {
		this.list = list;
		this.merger = merger;
	}

	@Override
	public List< Integer > list() {
		return list;
	}

	public ActorRef< Message > merger() {
		return merger;
	}
}
