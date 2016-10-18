package ph.test;

import ph.onethread.Actor;
import ph.onethread.Future;
import ph.onethread.Promise;

/**
 * TODO
 */
public class RootActor extends Actor implements Root {

	@Override
	public Future<String> serve(String input) {
		Foo foo1 = find(Foo.class, "1");
		Promise<String> p = promise();
		after(foo1.process(input), (r, e) -> {
			if (e == null) {
				p.set("root result: " + r, null);
			} else {
				p.set("root error: " + e, e);
			}
		});
		return p;
	}

	@Override
	public Future<String> frassle(String input) {
		throw new RuntimeException("ohno");
	}
}
