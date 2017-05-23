package org.fujaba.graphengine.algorithm;

import java.util.ArrayList;

import org.fujaba.graphengine.Match;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.pattern.PatternGraph;

public class Algorithm {
	
	private String name;
	private ArrayList<Algorithm> algorithmSteps;
	private PatternGraph atomicAlgorithm;
	private boolean repeating;
	
	public Algorithm(String name) {
		this.name = name;
		this.algorithmSteps = new ArrayList<Algorithm>();
		this.atomicAlgorithm = null;
		this.repeating = true;
	}
	
	public Application process(Graph input) {
		Graph output = input;
		if (atomicAlgorithm != null) {
			while (true) {
				ArrayList<Match> matches = PatternEngine.matchPattern(output, atomicAlgorithm, true);
				if (matches.size() > 0) {
					output = PatternEngine.applyMatch(matches.get(0));
//					System.err.println("\n" + atomicAlgorithm.getName());
//					System.err.println(output);
					if (!repeating) {
						break;
					}
				} else {
					break;
				}
			}
		} else {
			for (Algorithm algo: algorithmSteps) {
				while (true) {
					Graph subAlgorithmInput = output;
					Application subAlgorithmApplication = algo.process(subAlgorithmInput);
					if (subAlgorithmInput != subAlgorithmApplication.getOutput()) {
						output = subAlgorithmApplication.getOutput();
						System.err.println("\n" + subAlgorithmApplication.getAlgorithm().getName());
						System.err.println(output);
						if (!repeating) {
							break;
						}
					} else {
						break;
					}
				}
			}
		}
		return new Application(this, input, output);
	}
	
	public String getName() {
		return name;
	}
	public Algorithm setName(String name) {
		this.name = name;
		return this;
	}
	public ArrayList<Algorithm> getAlgorithmSteps() {
		return algorithmSteps;
	}
	public Algorithm setAlgorithmSteps(ArrayList<Algorithm> algorithmSteps) {
		this.algorithmSteps = algorithmSteps;
		return this;
	}
	public Algorithm addAlgorithmStep(Algorithm step) {
		this.algorithmSteps.add(step);
		return this;
	}
	public Algorithm addAlgorithmStep(PatternGraph step) {
		return addAlgorithmStep(step, true);
	}
	public Algorithm addAlgorithmStep(PatternGraph step, boolean repeating) {
		Algorithm newStep = new Algorithm(step.getName());
		newStep.setAtomicAlgorithm(step);
		newStep.setRepeating(repeating);
		this.algorithmSteps.add(newStep);
		return this;
	}
	public PatternGraph getAtomicAlgorithm() {
		return atomicAlgorithm;
	}
	public Algorithm setAtomicAlgorithm(PatternGraph atomicAlgorithm) {
		this.atomicAlgorithm = atomicAlgorithm;
		return this;
	}
	public boolean isRepeating() {
		return repeating;
	}
	public Algorithm setRepeating(boolean repeat) {
		this.repeating = repeat;
		return this;
	}

}
