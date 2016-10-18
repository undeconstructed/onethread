package onethread;

import java.lang.reflect.Method;

/**
 * TODO
 */
public class Invocation {

	final Object promise;
	final String self;
	final String key;
	final Class i;
	final Method method;
	final Object[] args;

	public Invocation(Object promise, String self, String key, Class i, Method method, Object[] args) {
		this.promise = promise;
		this.self = self;
		this.key = key;
		this.i = i;
		this.method = method;
		this.args = args;
	}
}
