package ph.onethread;

import ph.onethread.Actor.ContinuationFuture;

/**
 * Allows an actor to get external work done, via a controller.
 */
@SuppressWarnings("rawtypes")
public class Instruction {

	private final SignalTemplate signal;
	final ContinuationFuture f;
	private final Object args;

	Instruction(SignalTemplate signal, ContinuationFuture f, Object args) {
		this.signal = signal;
		this.f = f;
		this.args = args;
	}

	public SignalTemplate getSignal() {
		return signal;
	}

	/**
	 * @return the args
	 */
	public Object getArgs() {
		return args;
	}
}
