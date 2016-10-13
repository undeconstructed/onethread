package ph.test;

import ph.onethread.Future;

/**
 * TODO
 */
public interface Foo {

	public Future<String> process(String input);

	public void update(String anything);
}
