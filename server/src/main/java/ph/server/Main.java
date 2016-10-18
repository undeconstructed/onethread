package ph.server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import onethread.InstructionsAndResults;
import onethread.Platform;
import onethread.Result;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

/**
 * NanoHTTPD starts a thread for each connection, so we need to multiplex these together into a single brain. Might as
 * well use Java {@link Future}s.
 */
public class Main {

	// everything static to be lazy
	static Platform platform;
	static Map<String, CompletableFuture<String>> out = new HashMap<>();

	// synchronize to simply control access to the platform
	static synchronized Future<String> doWork(String in) {
		String id = UUID.randomUUID().toString();
		CompletableFuture<String> f = new CompletableFuture<>();
		try {
			InstructionsAndResults iar = platform.call(id, "ph.server.Actor1:root", "work", in);
			out.put(id, f);
			for (Result r : iar.getResults()) {
				CompletableFuture<String> f2 = out.remove(r.getKey());
				if (f2 != null) {
					f2.complete(String.valueOf(r.getResult()));
				}
			}
		} catch (Exception e) {
			f.completeExceptionally(e);
		}
		return f;
	}

	/**
	 * Initialize, run HTTP server, wait.
	 */
	public static void main(String[] args) throws Exception {
		platform = new Platform().addType(Actor1.class, x -> new Actor1Impl()).start();

		ServerRunner.executeInstance(new NanoHTTPD(8080) {
			public Response serve(IHTTPSession session) {
				try {
					return NanoHTTPD.newFixedLengthResponse(doWork(session.getUri()).get() + "\n");
				} catch (Exception e) {
					e.printStackTrace();
					return NanoHTTPD.newFixedLengthResponse("error " + e.getMessage() + "\n");
				}
			}
		});
	}
}
