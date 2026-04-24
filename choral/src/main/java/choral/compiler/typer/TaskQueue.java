package choral.compiler.typer;

import choral.compiler.Typer;
import choral.types.GroundReferenceType;
import choral.types.HigherClassOrInterface;
import choral.types.HigherReferenceType;
import choral.types.HigherTypeParameter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

/**
 * A priority queue of typechecking tasks, sorted by @Typer.Phase. We use the queue to ensure
 * that tasks in phase N are all completed before those in phase N+1.
 */
public class TaskQueue {

	public final Map< HigherClassOrInterface, Task > hierarchyConstructionTasks = new HashMap<>();

	public static class Task implements Comparable< Task >, Runnable {

		private final Phase phase;
		private final Runnable task;

		public Task( Phase phase, Runnable task ) {
			this.phase = phase;
			this.task = task;
		}

		/**
		 * Represents how many times isReady() was called when the task was not ready to be run.
		 * Is used to impose natural ordering on tasks with same phase
		 */
		int rounds = 0;

		@Override
		public int compareTo( Task o ) {
			int i = this.phase.compareTo( o.phase );
			if( i == 0 ) {
				i = Integer.compare( this.rounds, o.rounds );
			}
			return i;
		}

		@Override
		public void run() {
			if( status() == Task.Status.READY ) {
				status = Task.Status.PROCESSING;
				task.run();
				status = Task.Status.FINISHED;
			}
		}

		public void prepare() {
			if( status() == Task.Status.WAITING ) {
				if( isReady() ) {
					status = Task.Status.READY;
				} else {
					rounds += 1;
				}
			}
		}

		protected boolean isReady() {
			return true;
		}

		public enum Status {
			WAITING,
			READY,
			PROCESSING,
			FINISHED
		}

		protected Task.Status status = Task.Status.WAITING;

		public Task.Status status() {
			return status;
		}

	}

	public static class MemberTask extends TaskQueue.Task {

		private final HigherReferenceType type;

		public MemberTask( Phase phase, HigherReferenceType type, Runnable task ) {
			super( phase, task );
			this.type = type;
		}

		@Override
		public int compareTo( TaskQueue.Task o ) {
			int i = super.compareTo( o );
			if( i == 0 && o instanceof MemberTask ) {
				MemberTask m = (MemberTask) o;
				if( m.type.isStrictSubtypeOf( this.type ) ) {
					i = 1;
				} else if( this.type.isStrictSubtypeOf( m.type ) ) {
					i = -1;
				}
			}
			return i;
		}

		boolean dependenciesReady = false;

		@Override
		protected boolean isReady() {
			if( !dependenciesReady ) {
				if( type instanceof HigherTypeParameter ) {
					dependenciesReady = ( (HigherTypeParameter) type ).innerType()
							.upperBound().allMatch( GroundReferenceType::isInterfaceFinalised );
				} else {
					dependenciesReady = ( (HigherClassOrInterface) type ).innerType()
							.extendedClassesOrInterfaces()
							.allMatch( GroundReferenceType::isInterfaceFinalised );
				}
			}
			return dependenciesReady;
		}
	}


	PriorityQueue< Task > tasks = new PriorityQueue<>(
			Comparator.naturalOrder() );

	/**
	 * This method processes tasks in the priority queue up til the given phase.
	 */
	public void process( Phase to ) {
		while( !tasks.isEmpty() ) {
			// task at head of queue is of prior or same phase as "to"
			if( tasks.peek().phase.compareTo( to ) < 1 ) {
				Task task = tasks.poll();
				task.prepare();
				switch( task.status() ) {
					case READY -> task.run();
					case WAITING -> enqueue( task );
				}
			} else {
				// no more tasks for this and prior phases
				break;
			}
		}
	}

	public void process() {
		while( !tasks.isEmpty() ) {
			Task task = tasks.poll();
			task.prepare();
			switch( task.status() ) {
				case READY -> task.run();
				case WAITING -> enqueue( task );
			}
		}
	}

	/**
	 * Enqueues the given task, silently dropping it if the status of the task is not
	 * WAITING or READY.
	 */
	public void enqueue( Task t ) {
		if( t.status() == Task.Status.WAITING || t.status() == Task.Status.READY ) {
			tasks.add( t );
		}
	}

	/**
	 * Enqueues the given runnable to the priority queue as a task.
	 * @param p The phase during which the runnable should be run
	 * @param t The runnable enqueued as a task.
	 */
	public void enqueue( Phase p, Runnable t ) {
		enqueue( new Task( p, t ) );
	}

}
