package onethread;

/**
 * TODO
 */
class ProblemException extends Exception {

	final Problem problem;

	public ProblemException(Problem problem) {
		this.problem = problem;
	}
}
