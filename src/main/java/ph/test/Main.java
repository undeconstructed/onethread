package ph.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ph.onethread.Output;
import ph.onethread.Platform;

/**
 * TODO
 */
public class Main {

	static class Pending {

		int number;
		Output output;

		public Pending(int number, Output output) {
			this.number = number;
			this.output = output;
		}
	}

	public static void main(String[] args) throws Exception {
		Platform platform = new Platform().addType(Root.class, RootActor.class).addType(Foo.class, FooActor.class)
				.start();

		System.out.println("# ph.test.Root 1 frobnicate test");
		System.out.println("# ph.test.Root 1 frassle test");

		List<Pending> pending = new LinkedList<>();

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		for (int i = 1;; i++) {
			System.out.format("[%d] $ ", i);
			line = in.readLine();
			if (line == null) {
				break;
			}
			String[] ss = line.split(" ");
			Output o = platform.call(ss[0], ss[1], ss[2], ss[3]);
			pending.add(new Pending(i, o));
			Pending p;
			for (Iterator<Pending> it = pending.iterator(); it.hasNext();) {
				p = it.next();
				if (p.output.isReady()) {
					try {
						System.out.format("[%d] > %s\n", p.number, p.output.get());
					} catch (Exception e) {
						System.out.format("[%d] ! %s\n",  p.number, e.getMessage());
					}
					it.remove();
				}
			}
		}
	}
}
