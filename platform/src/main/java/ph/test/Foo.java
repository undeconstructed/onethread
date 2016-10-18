package ph.test;

import onethread.Future;

/**
 * TODO
 */
public interface Foo {

	public Future<String> process(String input);

	public void signal(String anything);
}
