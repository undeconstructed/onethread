package ph.test;

import onethread.Future;

/**
 * TODO
 */
public interface Root {

	public Future<String> serve(String input);

	public Future<String> frassle(String input);
}
