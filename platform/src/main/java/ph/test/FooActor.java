package ph.test;

import onethread.Actor;
import onethread.Future;
import onethread.Promise;

/**
 * TODO
 */
public class FooActor extends Actor implements Foo {

	private String input;
	private Promise<String> promise;

	@Override
	public Future<String> process(String input) {
		this.input = input;
		this.promise = promise();
		ignore(instruct("test"));
		return promise;
	}

	@Override
	public void signal(String update) {
		if (promise != null) {
			promise.set(input + "+" + update, null);
			promise = null;
		}
	}
}
