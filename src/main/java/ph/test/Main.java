package ph.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ph.onethread.Instruction;
import ph.onethread.Output;
import ph.onethread.Platform;

class MainTask {

	final Command command;
	final Signal signal;

	public MainTask(Command command, Signal signal) {
		this.command = command;
		this.signal = signal;
	}
}

class Command {

	final String string;

	public Command(String string) {
		this.string = string;
	}
}

class Signal {
}

/**
 * {@link Controller} is the brain, and the brain is a onethread setup.
 */
class Controller implements Runnable {

	BlockingQueue<MainTask> queue = new LinkedBlockingQueue<>();

	/**
	 * @param randomWork
	 */
	public Controller(RandomWork randomWork) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {

		Platform platform = new Platform().addType(Root.class, RootActor.class).addType(Foo.class, FooActor.class)
				.start();

		while (true) {
			try {
				MainTask task = queue.take();
				if (task.command != null) {
					List<Instruction> is = platform.call("xxx", "ph.test.Root", "1", "serve", task.command.string);
					for (Instruction i : is) {
						// TODO - act on i in a way that gets the result back in to wherever
					}
				} else if (task.signal != null) {
				}
			} catch (InterruptedException e) {
				return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void command(String string) throws InterruptedException {
		queue.put(new MainTask(new Command(string), null));
	}
}

/**
 * {@link RandomWork} takes instructions them randomly says that it has carried them out some time later.
 */
class RandomWork implements Runnable {

	@Override
	public void run() {
	}
}

/**
 * {@link ServerThing} takes user input and returns answers.
 */
class ServerThing implements Runnable {

	private Controller controller;

	static class Pending {

		int number;
		Output output;

		public Pending(int number, Output output) {
			this.number = number;
			this.output = output;
		}
	}

	public ServerThing(Controller controller) {
		this.controller = controller;
	}

	@Override
	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		for (int i = 1;; i++) {
			try {
				System.out.format("[%d] $ ", i);
				line = in.readLine();
				if (line == null) {
					break;
				}
				controller.command(line);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				return;
			}
		}
	}
}

/**
 * TODO
 */
public class Main {

	public static void main(String[] args) throws Exception {

		RandomWork randomWork = new RandomWork();
		Controller controller = new Controller(randomWork);
		ServerThing inputThing = new ServerThing(controller);

		// explicitly threaded things
		new Thread(controller).start();
		new Thread(randomWork).start();
		new Thread(inputThing).start();

		// main thread is no use to us
		new Object().wait();
	}
}
