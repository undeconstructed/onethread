package ph.onethread;

/**
 * TODO
 */
public interface Continuation<T> {

	@SuppressWarnings("rawtypes")
	static final Continuation NOP = (r, e) -> {
	};

	@SuppressWarnings("unchecked")
	public static <T> Continuation<T> nop() {
		return NOP;
	}

	public void apply(T r, Problem p);
}
