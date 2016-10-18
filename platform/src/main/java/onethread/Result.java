package onethread;

/**
 * TODO
 */
public class Result {

	private final String key;
	private final Object result;
	private final Problem problem;

	public Result(String key, Object result, Problem problem) {
		this.key = key;
		this.result = result;
		this.problem = problem;
	}

	public String getKey() {
		return key;
	}

	public Object getResult() {
		return result;
	}

	public Problem getProblem() {
		return problem;
	}
}
