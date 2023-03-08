package Benchmarks.Akka.MergeSort;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.ArrayList;
import java.util.List;

public class SortingLocation extends AbstractBehavior< Message > {

	public SortingLocation( ActorContext< Message > context ) {
		super( context );
	}

	public static Behavior< Message > create() {
		System.out.println( "Created SortingLocation" );
		return Behaviors.setup( SortingLocation::new );
	}

	@Override
	public Receive< Message > createReceive() {
		return newReceiveBuilder()
				.onMessage( SortMessage.class, this::onSort )
				.onMessage( MergeMessage.MergeLeftMessage.class, this::onMerge )
				.onMessage( MergeMessage.MergeRightMessage.class, this::onMerge )
				.build();
	}

	private ActorRef< Message > merger;

	private Behavior< Message > onSort( SortMessage message ) {
		System.out.println( "Received sorting request " + message.list() );
		List< Integer > l = message.list();
		merger = message.merger();

		if( l.size() > 2 ) {
			int pivot = l.size() / 2;
			getContext().spawn( LeftSortingLocation.create(), "Location_B" ).tell(
					new SortMessage( l.subList( 0, pivot ), getContext().getSelf() ) );
			getContext().spawn( RightSortingLocation.create(), "Location_C" ).tell(
					new SortMessage( l.subList( pivot, l.size() ), getContext().getSelf() ) );
		} else {
			if( l.size() < 2 ) {
				message.merger().tell( getSpecificMergeMessage( l ) );
			} else {
				message.merger().tell( getSpecificMergeMessage(
						l.get( 0 ) > l.get( 1 ) ? List.of( l.get( 1 ), l.get( 0 ) ) : l ) );
			}
		}
		return this;
	}

	private List< Integer > merge( List< Integer > lhs, List< Integer > rhs ) {
		if( lhs.size() < 1 ) {
			return rhs;
		}
		if( rhs.size() < 1 ) {
			return lhs;
		}
		List< Integer > l = new ArrayList<>();
		if( lhs.get( 0 ) > rhs.get( 0 ) ) {
			l.add( rhs.get( 0 ) );
			l.addAll( merge( lhs, rhs.subList( 1, rhs.size() ) ) );
		} else {
			l.add( lhs.get( 0 ) );
			l.addAll( merge( lhs.subList( 1, lhs.size() ), rhs ) );
		}
		return l;
	}

	private List< Integer > lhs, rhs;

	private Behavior< Message > onMerge( MergeMessage.MergeLeftMessage message ) {
		lhs = message.list();
		checkMerge();
		return this;
	}

	private Behavior< Message > onMerge( MergeMessage.MergeRightMessage message ) {
		rhs = message.list();
		checkMerge();
		return this;
	}

	private void checkMerge() {
		if( rhs != null && lhs != null ) {
			merger.tell( getSpecificMergeMessage( merge( lhs, rhs ) ) );
		}
	}

	public MergeMessage getSpecificMergeMessage( List< Integer > l ) {
		return new MergeMessage( l );
	}


	public static class RightSortingLocation extends SortingLocation {

		public RightSortingLocation( ActorContext< Message > context ) {
			super( context );
		}

		public static Behavior< Message > create() {
			System.out.println( "Created RightSortingLocation" );
			return Behaviors.setup( RightSortingLocation::new );
		}

		@Override
		public MergeMessage getSpecificMergeMessage( List< Integer > l ) {
			return new MergeMessage.MergeRightMessage( l );
		}
	}


	public static class LeftSortingLocation extends SortingLocation {

		public LeftSortingLocation( ActorContext< Message > context ) {
			super( context );
		}

		public static Behavior< Message > create() {
			System.out.println( "Created LeftSortingLocation" );
			return Behaviors.setup( LeftSortingLocation::new );
		}

		@Override
		public MergeMessage getSpecificMergeMessage( List< Integer > l ) {
			return new MergeMessage.MergeLeftMessage( l );
		}
	}
}
