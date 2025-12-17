package choral.exceptions;

import java.util.List;

import choral.ast.Position;

public class ChoralCompoundException extends ChoralException {

	private final List< ? extends ChoralException > causes;

	public ChoralCompoundException( List< ? extends ChoralException > causes ) {
		this.causes = causes;
	}

	public List< ? extends ChoralException > getCauses() {
		return this.causes;
	}

}
