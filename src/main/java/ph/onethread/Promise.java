package ph.onethread;

/**
 * TODO
 */
public abstract class Promise<T> extends Future<T> {

	public abstract void set(T r, Problem e);
}
