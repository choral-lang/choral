package choral.examples.RetwisChoral.emitters;

import choral.examples.RetwisChoral.inMemoryImpl.InMemoryCommandInterface;

public class InMemoryEmitter implements Emitter {

	private final InMemoryCommandInterface commandInterface;

	private InMemoryEmitter( InMemoryCommandInterface commandInterface ) {
		this.commandInterface = commandInterface;
	}

	public static InMemoryEmitter use( InMemoryCommandInterface commandInterface ) {
		return new InMemoryEmitter( commandInterface );
	}

	public Emitter emit( Action action ){
		commandInterface.addAction( action );
		return this;
	}

}
