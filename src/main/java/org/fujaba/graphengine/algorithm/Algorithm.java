package org.fujaba.graphengine.algorithm;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.Match;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.pattern.PatternGraph;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class Algorithm {
	
	private String name;
	private ArrayList<Algorithm> algorithmSteps;
	private PatternGraph atomicAlgorithm;
	private boolean repeating;
	
	public Algorithm(String name) {
		this.name = name;
		this.algorithmSteps = new ArrayList<Algorithm>();
		this.atomicAlgorithm = null;
		this.repeating = false;
	}
	
	public static Algorithm loadFrom(String path) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		return GraphEngine.getGson().fromJson(new FileReader(path), Algorithm.class);
	}
	
	public Algorithm saveTo(String path) throws JsonIOException, IOException {
		FileWriter fw = new FileWriter(path);
		GraphEngine.getGson().toJson(this, fw);
		fw.flush();
		fw.close();
		return this;
	}
	
	public Application process(Graph input) {
		Graph output = input;
		if (atomicAlgorithm != null) {
			
//			String test = GraphEngine.getGson().toJson(atomicAlgorithm);
//			System.err.println(test);
//			PatternGraph testPG = GraphEngine.getGson().fromJson(test, PatternGraph.class);
//			test = GraphEngine.getGson().toJson(testPG);
//			System.err.println(test + "\n");
		
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
//						System.err.println("\n" + subAlgorithmApplication.getAlgorithm().getName());
//						System.err.println(output);
						if (!algo.isRepeating()) {
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
	public Algorithm addAlgorithmStep(Algorithm step, boolean repeating) {
		step.setRepeating(repeating);
		this.algorithmSteps.add(step);
		return this;
	}
	public Algorithm addAlgorithmStep(PatternGraph step) {
		return addAlgorithmStep(step, false);
	}
	public Algorithm addAlgorithmStep(PatternGraph step, boolean repeating) {
		Algorithm newStep = new Algorithm("GTR: " + step.getName());
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
	public Algorithm setRepeating(boolean repeating) {
		this.repeating = repeating;
		return this;
	}
	
	public String toString() {
		return GraphEngine.getGson().toJson(this);
	}

}
