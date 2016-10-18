package ph.onethread;

import ph.onethread.Actor.ContinuationFuture;

/**
 * Allows an actor to get external work done, via a controller.
 */
@SuppressWarnings("rawtypes")
public class Instruction {

	private ContinuationFuture f;
	private Object args;

	Instruction(ContinuationFuture f, Object args) {
		this.f = f;
		this.args = args;
	}

	/**
	 * @return the args
	 */
	public Object getArgs() {
		return args;
	}
}
