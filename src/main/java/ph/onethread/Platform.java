package ph.onethread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;

/**
 * TODO
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Platform {

	/**
	 * TODO
	 */
	private static class Task {

		String desc;
		Callable<List<Instruction>> work;

		public Task(String desc, Callable<List<Instruction>> work) {
			this.desc = desc;
			this.work = work;
		}
	}

	/**
	 * TODO
	 */
	private class Output {

		private final String xxx;

		public Output(String xxx) {
			this.xxx = xxx;
		}

		void set(Object r, Problem e) {
			results.add(new Result(xxx, r, e));
		}
	}

	private Map<Class, Class> actorTypes;

	private Map<String, Actor> actors;
	private Queue<Task> tasks;
	private List<Result> results;

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
		results = new LinkedList<>();
		return this;
	}

	/**
	 * Trigger an invocation.
	 * 
	 * @param input
	 */
	public InstructionsAndResults call(String xxx, String key, String method, String input) throws Exception {
		if (actors == null) {
			throw new RuntimeException("not started");
		}
		Class i = Class.forName(key.substring(0, key.indexOf(':')));
		call(new Output(xxx), "user", key, i, i.getMethod(method, String.class), new Object[] { input });

		return runOn();
	}

	public InstructionsAndResults signal(String key, String method, String input) throws Exception {
		if (actors == null) {
			throw new RuntimeException("not started");
		}
		Class i = Class.forName(key.substring(0, key.indexOf(':')));
		signal("user", key, i, i.getMethod(method, String.class), new Object[] { input });

		return runOn();
	}

	private InstructionsAndResults runOn() throws Exception {
		List<Instruction> instructions = new LinkedList<>();
		while (!tasks.isEmpty()) {
			// System.out.println("tasks left: " + tasks.size());
			Task task = tasks.poll();
			System.out.println("running: " + task.desc);
			instructions.addAll(task.work.call());
		}
		List<Result> rs = this.results;
		if (!rs.isEmpty()) {
			this.results = new LinkedList<>();
		}
		return new InstructionsAndResults(instructions, rs);
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
		Callable<List<Instruction>> work = () -> {
			Actor a = actors.get(key);
			if (a == null) {
				try {
					a = (Actor) actorTypes.get(i).newInstance();
				} catch (Exception e) {
					set(promise, null, new Problem("actor creation error: " + e.getMessage()));
					return Collections.emptyList();
				}
				actors.put(key, a);
				a.setup(this, i, key);
			}
			if (a.isAvailable()) {
				try {
					Future future = (Future) method.invoke(a, args);
					if (future instanceof ValueFuture) {
						ValueFuture rf = (ValueFuture) future;
						a.verify(null);
						set(promise, rf.value, null);
						return a.anyInstructions();
					} else {
						Actor.ContinuationFuture c = (Actor.ContinuationFuture) future;
						a.verify(c);
						c.setListener((r, e) -> {
							set(promise, r, e);
						});
						return a.anyInstructions();
					}
				} catch (InvocationTargetException e) {
					set(promise, null, new Problem("actor call error: " + e.getCause().getMessage()));
				} catch (Exception e) {
					set(promise, null, new Problem("unknown actor error: " + e.getMessage()));
				}
			} else {
				a.enqueue(new Invocation(promise, self, key, i, method, args));
			}
			return Collections.emptyList();
		};
		tasks.add(new Task(self + " call " + key + " " + method.getName(), work));
	}

	void signal(String self, String key, Class i, Method method, Object[] args) {
		Callable<List<Instruction>> work = () -> {
			Actor a = actors.get(key);
			if (a == null) {
				try {
					a = (Actor) actorTypes.get(i).newInstance();
				} catch (Exception e) {
					System.out.println("actor creation error: " + e.getMessage());
					return Collections.emptyList();
				}
				actors.put(key, a);
				a.setup(this, i, key);
			}
			try {
				method.invoke(a, args);
				return a.anyInstructions();
			} catch (InvocationTargetException e) {
				System.out.println("actor signal error: " + e.getCause().getMessage());
			} catch (Exception e) {
				System.out.println("unknown actor error:" + e.getMessage());
			}
			return Collections.emptyList();
		};
		tasks.add(new Task(self + " signal " + key + " " + method.getName(), work));
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
				// TODO - this is wrong
				return Collections.emptyList();
			}));
		} else if (promise instanceof Output) {
			System.out.format("setting output: %s, %s%n", e, r);
			((Output) promise).set(r, e);
		}
	}
}
