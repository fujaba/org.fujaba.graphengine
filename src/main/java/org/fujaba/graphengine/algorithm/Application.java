package org.fujaba.graphengine.algorithm;

import org.fujaba.graphengine.graph.Graph;

public class Application {
	
	private Algorithm algorithm;
	private Graph input;
	private Graph output;
	
	public Application(Algorithm algorithm, Graph input, Graph output) {
		this.algorithm = algorithm;
		this.input = input;
		this.output = output;
	}
	
	public Algorithm getAlgorithm() {
		return algorithm;
	}
	public Application setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
		return this;
	}
	public Graph getInput() {
		return input;
	}
	public Application setInput(Graph input) {
		this.input = input;
		return this;
	}
	public Graph getOutput() {
		return output;
	}
	public Application setOutput(Graph output) {
		this.output = output;
		return this;
	}

}
