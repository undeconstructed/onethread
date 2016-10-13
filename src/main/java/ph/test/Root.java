package ph.test;

import ph.onethread.Future;

/**
 * TODO
 */
public interface Root {

	public Future<String> frobnicate(String input);

	public Future<String> frassle(String input);
}
