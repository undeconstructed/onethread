package ph.onethread;

import java.util.List;

/**
 * TODO
 */
public class InstructionsAndResults {

	private final List<Instruction> instructions;
	private final List<Result> results;

	public InstructionsAndResults(List<Instruction> instructions, List<Result> results) {
		this.instructions = instructions;
		this.results = results;
	}

	public List<Instruction> getInstructions() {
		return instructions;
	}

	public List<Result> getResults() {
		return results;
	}
}
