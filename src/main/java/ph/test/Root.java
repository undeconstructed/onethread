package ph.test;

import ph.onethread.Future;

/**
 * TODO
 */
public interface Root {

	public Future<String> serve(String input);

	public Future<String> frassle(String input);
}
