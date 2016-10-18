package ph.server;

import onethread.Actor;

/**
 * TODO
 */
public class Actor1Impl extends Actor implements Actor1 {

	@Override
	public onethread.Future<String> work(String data) {
		return value("ok " + data);
	}
}
