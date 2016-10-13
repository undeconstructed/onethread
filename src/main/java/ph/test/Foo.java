package ph.test;

import ph.onethread.Future;

public interface Foo {

	public Future<String> process(String input);
}
