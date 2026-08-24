package supplement;

import java.util.List;

public abstract class LiftedBase implements LiftedFunction<String, Number, RuntimeException> {
	@Override
	public RuntimeException apply(String input, List<Number> nested) {
		return new RuntimeException();
	}
}
