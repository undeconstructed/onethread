package ph.onethread;

/**
 * TODO
 */
public class Output<T> {

	private boolean set;
	private T r;
	private Problem e;

	void set(T r, Problem e) {
		this.set = true;
		this.r = r;
		this.e = e;
	}

	public boolean isReady() {
		return set;
	}

	public T get() {
		if (e != null) {
			throw new RuntimeException("actor problem: " + e.toString());
		}
		return r;
	}
}
