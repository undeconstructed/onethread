package ph.onethread;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Sort of actor.
 */
public class Actor {

	/**
	 * A future held inside an actor for managing a continuation.
	 */
	class ContinuationFuture<T> extends Promise<T> {

		final String desc;
		Continuation<T> listener;

		public ContinuationFuture(String desc) {
			this.desc = desc;
		}

		void setListener(Continuation<T> listener) {
			if (this.listener != null)
				throw new IllegalStateException("already has listener");
			this.listener = listener;
		}

		public void set(T r, Problem e) {
			removeContinuation(this);
			listener.apply(r, e);
		}

		@Override
		public String toString() {
			return desc;
		}
	}

	// link back to platform
	Platform platform;
	// own key
	String key;
	// pending continuations
	List<ContinuationFuture> continuations = new LinkedList<>();
	// work the actor wants to see done elsewhere
	List<Instruction> instructions = new LinkedList<>();
	// pending new work
	Queue<Invocation> invocations = new LinkedList<>();

	/**
	 * Tells the actor about its environment.
	 * 
	 * @param platform
	 * @param key
	 */
	final void setup(Platform platform, String key) {
		this.platform = platform;
		this.key = key;
	}

	/**
	 * Reports whether this actor can accept new input.
	 */
	final boolean isAvailable() {
		return continuations.isEmpty();
	}

	/**
	 * Checks if an actor call has left any futures with no listeners, with the exception of the one it returned.
	 * 
	 * @param returned
	 */
	final void verify(ContinuationFuture returned) {
		for (ContinuationFuture pf : continuations) {
			if (returned != pf && pf.listener == null) {
				throw new IllegalStateException("future has no listener in " + key);
			}
		}
		if (returned != null && returned.listener != null) {
			throw new IllegalStateException("returned future has a listener in " + key);
		}
	}

	/**
	 * @param invocation
	 */
	final void enqueue(Invocation invocation) {
		invocations.add(invocation);
	}

	/**
	 * @return
	 */
	final List<Instruction> anyInstructions() {
		if (!instructions.isEmpty()) {
			List<Instruction> t = instructions;
			instructions = new LinkedList<>();
			return t;
		}
		return Collections.emptyList();
	}

	/**
	 * To return an immediate result.
	 * 
	 * @param t
	 * @return
	 */
	protected final <T> Future<T> value(T t) {
		return new ValueFuture(t);
	}

	protected final <T> Future<T> resultOf(Future<T> f) {
		return f;
	}

	/**
	 * To return a result that will be filled in later.
	 * 
	 * @return
	 */
	protected final <T> Promise<T> promise() {
		ContinuationFuture<T> p = new ContinuationFuture<>(key + " promises");
		continuations.add(p);
		return p;
	}

	/**
	 * Do something after a future is ready.
	 * 
	 * @param f
	 * @param onSuccess
	 * @param onFailure
	 */
	protected <T> void after(Future<T> f, Continuation<T> c) {
		if (f instanceof ContinuationFuture) {
			ContinuationFuture<T> pf = (ContinuationFuture<T>) f;
			pf.setListener((r, e) -> {
				removeContinuation(f);
				c.apply(r, e);
			});
		} else {
			throw new RuntimeException("cannot continue " + f.getClass().getCanonicalName());
		}
	}

	/**
	 * Explicitly say it doesn't matter if a future succeeds.
	 * 
	 * @param f
	 * @return
	 */
	protected <T> void ignore(Future<T> f) {
		removeContinuation(f);
	}

	private final void removeContinuation(Future f) {
		if (continuations.remove(f) && continuations.isEmpty()) {
			Invocation i = invocations.poll();
			if (i != null) {
				platform.admit(i);
			}
		}
	}

	/**
	 * Set a follow on action after a future is done.
	 * 
	 * @param f
	 * @param onSuccess
	 * @param onFailure
	 * @return
	 */
	protected <T, T1> Future<T> follow(Future<T> f, Function<T, T1> onSuccess, Function<Throwable, T1> onFailure) {
		return null;
	}

	/**
	 * Sends an instruction back up to the controller, probably to get some external work done.
	 * 
	 * @param args
	 * @return
	 */
	protected final <T> Future<T> instruct(Object args) {
		ContinuationFuture f = new ContinuationFuture<>(key + " after instruction");
		continuations.add(f);
		instructions.add(new Instruction(f, args));
		return f;
	}

	protected final <I> I find(Class<I> i, String id) {
		return (I) Proxy.newProxyInstance(Actor.class.getClassLoader(), new Class[] { i }, (proxy, method, args) -> {
			String target = i.getCanonicalName() + ":" + id;
			ContinuationFuture f = new ContinuationFuture<>(key + " after " + target + " " + method.getName());
			continuations.add(f);
			platform.call(f, key, target, i, method, args);
			return f;
		});
	}
}
