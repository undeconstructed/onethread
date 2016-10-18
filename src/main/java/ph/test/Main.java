package ph.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ph.onethread.Instruction;
import ph.onethread.InstructionsAndResults;
import ph.onethread.Platform;
import ph.onethread.Result;
import ph.onethread.SignalTemplate;

class MainTask {

	final Command command;
	final Signal signal;

	public MainTask(Command command, Signal signal) {
		this.command = command;
		this.signal = signal;
	}
}

class Command {

	final String number;
	final String string;

	public Command(String number, String string) {
		this.number = number;
		this.string = string;
	}
}

class Signal {

	final SignalTemplate signal;
	final String data;

	public Signal(SignalTemplate signal, String data) {
		this.signal = signal;
		this.data = data;
	}

	public String getData() {
		return data;
	}
}

/**
 * {@link Controller} is the brain, and the brain is a onethread setup.
 */
class Controller implements Runnable {

	private BlockingQueue<MainTask> queue = new LinkedBlockingQueue<>();
	private RandomWork worker;

	@Override
	public void run() {

		Platform platform = new Platform().addType(Root.class, (x) -> new RootActor())
				.addType(Foo.class, (x) -> new FooActor()).start();

		while (true) {
			try {
				MainTask task = queue.take();
				if (task.command != null) {
					process(platform.call(task.command.number, "ph.test.Root:1", "serve", task.command.string));
				} else if (task.signal != null) {
					SignalTemplate s = task.signal.signal;
					process(platform.signal(s.getKey(), s.getMethod(), task.signal.getData()));
				}
			} catch (InterruptedException e) {
				return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void process(InstructionsAndResults iar) {
		for (Instruction i : iar.getInstructions()) {
			worker.perform(i.getSignal(), i.getArgs());
		}
		for (Result r : iar.getResults()) {
			if (r.getProblem() != null) {
				System.out.format("[%s] ! %s\n", r.getKey(), r.getProblem());
			} else {
				System.out.format("[%s] > %s\n", r.getKey(), r.getResult());
			}
		}
	}

	public void command(int n, String string) throws InterruptedException {
		queue.put(new MainTask(new Command(Integer.toString(n), string), null));
	}

	public void signal(SignalTemplate signal, String data) throws InterruptedException {
		queue.put(new MainTask(null, new Signal(signal, data)));
	}

	public void addWorker(RandomWork randomWork) {
		this.worker = randomWork;
	}
}

/**
 * {@link RandomWork} takes instructions them randomly says that it has carried them out some time later.
 */
class RandomWork implements Runnable {

	private final Controller controller;
	List<SignalTemplate> work = new ArrayList<>();
	Random rand = new Random();

	public RandomWork(Controller controller) {
		this.controller = controller;
	}

	public synchronized void perform(SignalTemplate signal, Object object) {
		work.add(signal);
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(rand.nextInt(5000) + 2000);
				synchronized (this) {
					if (!work.isEmpty()) {
						int r = rand.nextInt(work.size());
						SignalTemplate signal = work.remove(r);
						System.out.println("working for " + signal);
						controller.signal(signal, "random-string");
					}
				}
			} catch (InterruptedException e) {
				return;
			}
		}
	}
}

/**
 * {@link ServerThing} takes user input and returns answers.
 */
class ServerThing implements Runnable {

	private Controller controller;

	public ServerThing(Controller controller) {
		this.controller = controller;
	}

	@Override
	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		for (int i = 1;;) {
			try {
				System.out.format("[%d] $ ", i);
				line = in.readLine();
				if (line == null) {
					continue;
				}
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				controller.command(i, line);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				return;
			}
			i++;
		}
	}
}

/**
 * TODO
 */
public class Main {

	public static void main(String[] args) throws Exception {

		// controller contains the brain
		Controller controller = new Controller();

		// things that will be part of the body
		RandomWork randomWork = new RandomWork(controller);
		ServerThing inputThing = new ServerThing(controller);

		// let the brain know about the body
		controller.addWorker(randomWork);

		// explicitly threaded things
		new Thread(controller).start();
		new Thread(randomWork).start();
		new Thread(inputThing).start();

		// main thread is no use to us
		synchronized (Main.class) {
			Main.class.wait();
		}
	}
}
