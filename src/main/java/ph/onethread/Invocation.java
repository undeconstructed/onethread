package ph.onethread;

import java.lang.reflect.Method;

/**
 * TODO
 */
public class Invocation {

	final Method method;
	final Object[] args;

	public Invocation(Method method, Object[] args) {
		this.method = method;
		this.args = args;
	}
}
