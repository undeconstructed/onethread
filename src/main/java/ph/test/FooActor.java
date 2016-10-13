package ph.test;

import ph.onethread.Actor;
import ph.onethread.Future;

/**
 * TODO
 */
public class FooActor extends Actor implements Foo {

	@Override
	public Future<String> process(String input) {
		return value("processed: " + input);
	}
}
