package ph.onethread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

class Task {

	String desc;
	Runnable work;

	public Task(String desc, Runnable work) {
		this.desc = desc;
		this.work = work;
	}
}

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Platform {

	private Map<Class, Class> actorTypes;

	private Map<String, Actor> actors;
	private Queue<Task> tasks;

	public Platform() {
		actorTypes = new HashMap<>();
	}

	public <I, T extends I> Platform addType(Class<I> i, Class<T> t) {
		if (actors != null) {
			throw new RuntimeException("started");
		}
		actorTypes.put(i, t);
		return this;
	}

	/**
	 * Locks in config and does some setup.
	 * 
	 * @return
	 */
	public Platform start() {
		if (actors != null) {
			throw new RuntimeException("started");
		}
		actors = new HashMap<>();
		tasks = new LinkedList<>();
		return this;
	}

	/**
	 * Trigger an invocation.
	 * 
	 * @param input
	 */
	public Output call(String type, String id, String method, String input) throws Exception {
		if (actors == null) {
			throw new RuntimeException("not started");
		}
		Class i = Class.forName(type);
		String key = i.getCanonicalName() + ":" + id;
		Output o = new Output<>();
		call(o, "user", key, i, i.getMethod(method, String.class), new Object[] { input });

		runOn();

		return o;
	}

	public void signal(String type, String id, String method, String input) throws Exception {
		if (actors == null) {
			throw new RuntimeException("not started");
		}
		Class i = Class.forName(type);
		String key = i.getCanonicalName() + ":" + id;
		signal("user", key, i, i.getMethod(method, String.class), new Object[] { input });

		runOn();
	}

	private void runOn() {
		while (!tasks.isEmpty()) {
			// System.out.println("tasks left: " + tasks.size());
			Task task = tasks.poll();
			System.out.println("running: " + task.desc);
			task.work.run();
		}
	}

	/**
	 * Internal call method, for tying actors together.
	 * 
	 * @param promise
	 * @param key
	 * @param i
	 * @param method
	 * @param args
	 */
	void call(Object promise, String self, String key, Class i, Method method, Object[] args) {
		tasks.add(new Task(self + " call " + key + " " + method.getName(), () -> {
			Actor a = actors.get(key);
			if (a == null) {
				try {
					a = (Actor) actorTypes.get(i).newInstance();
				} catch (Exception e) {
					set(promise, null, new Problem("actor creation error: " + e.getMessage()));
					return;
				}
				actors.put(key, a);
				a.setup(this, key);
			}
			if (a.isAvailable()) {
				try {
					Future future = (Future) method.invoke(a, args);
					if (future instanceof ValueFuture) {
						ValueFuture rf = (ValueFuture) future;
						a.verify(null);
						set(promise, rf.value, null);
					} else {
						Actor.ContinuationFuture c = (Actor.ContinuationFuture) future;
						a.verify(c);
						c.setListener((r, e) -> {
							set(promise, r, e);
						});
					}
				} catch (InvocationTargetException e) {
					set(promise, null, new Problem("actor call error: " + e.getCause().getMessage()));
				} catch (Exception e) {
					set(promise, null, new Problem("unknown actor error: " + e.getMessage()));
				}
			} else {
				a.enqueue(new Invocation(promise, self, key, i, method, args));
			}
		}));
	}

	void signal(String self, String key, Class i, Method method, Object[] args) {
		tasks.add(new Task(self + " signal " + key + " " + method.getName(), () -> {
			Actor a = actors.get(key);
			if (a == null) {
				try {
					a = (Actor) actorTypes.get(i).newInstance();
				} catch (Exception e) {
					System.out.println("actor creation error: " + e.getMessage());
					return;
				}
				actors.put(key, a);
				a.setup(this, key);
			}
			try {
				method.invoke(a, args);
			} catch (InvocationTargetException e) {
				System.out.println("actor signal error: " + e.getCause().getMessage());
			} catch (Exception e) {
				System.out.println("unknown actor error:" + e.getMessage());
			}
		}));
	}

	/**
	 * Puts a queued invocation back into the system.
	 * 
	 * @param i
	 */
	void admit(Invocation i) {
		call(i.promise, i.self, i.key, i.i, i.method, i.args);
	}

	/**
	 * @param promise
	 * @param value
	 * @param object
	 */
	private void set(Object promise, Object r, Problem e) {
		if (promise instanceof Actor.ContinuationFuture) {
			tasks.add(new Task("continue " + promise, () -> {
				((Actor.ContinuationFuture) promise).set(r, e);
			}));
		} else if (promise instanceof Output) {
			((Output) promise).set(r, e);
		}
	}
}
