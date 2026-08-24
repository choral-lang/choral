package supplement;

import java.util.List;

public interface LiftedFunction<T, U, R> {
	R apply(T input, List<U> nested);
}
