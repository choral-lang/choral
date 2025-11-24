package choral.exceptions;

import java.util.ArrayList;
import java.util.List;

import choral.ast.Position;

public class ChoralCompoundException extends ChoralException {

	private final List< ? extends ChoralException > causes;
	private final List<Position> positions;

	public ChoralCompoundException( List< ? extends ChoralException > causes ) {
		this.causes = causes;
		positions = new ArrayList<>();
	}

	public ChoralCompoundException( List< ? extends ChoralException > causes, List<Position> positions ) {
		this.causes = causes;
		this.positions = positions;
	}

	public List< ? extends ChoralException > getCauses() {
		return this.causes;
	}

	public List<Position> getPositions() {
		return this.positions;
	}

}
