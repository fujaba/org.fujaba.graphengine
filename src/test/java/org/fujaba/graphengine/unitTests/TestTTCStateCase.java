package org.fujaba.graphengine.unitTests;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.Match;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.algorithm.Algorithm;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.pattern.PatternAttribute;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;
import org.fujaba.graphengine.stateelimination.TTCStateCaseGraphLoader;
import org.junit.Assert;
import org.junit.Test;

public class TestTTCStateCase {

	private static final String taskMainPath = "src/main/resources/ExperimentalData/testdata/emf/task-main/";
	private static final String[] fileNamesTaskMain = {
			"leader3_2.xmi",
			"leader4_2.xmi",
			"leader3_3.xmi",
			"leader5_2.xmi",
			"leader3_4.xmi",
			"leader3_5.xmi",
			"leader4_3.xmi",
			"leader6_2.xmi",
			"leader3_6.xmi",
			"leader4_4.xmi",
			"leader5_3.xmi",
			"leader3_8.xmi",
			"leader4_5.xmi",
			"leader6_3.xmi",
			"leader4_6.xmi",
			"leader5_4.xmi",
			"leader5_5.xmi",
			"leader6_4.xmi",
			"leader6_5.xmi"
	};

	private static final String taskExtension1Path = "src/main/resources/ExperimentalData/testdata/emf/task-extension1/";
	private static final String[] fileNamesTaskExtension1 = {
			"zeroconf.xmi"
	};

	@Test
	public void testSolvingTTC2017StateEliminationCaseWithAlgorithm() {
		Algorithm algorithmStateCaseTTC2017 = getStateCaseAlgorithmTTC2017();
		String[] fileNamesTaskMain = {"leader3_2.xmi"};
		for (String fileName: fileNamesTaskMain) {
			System.out.println("TTC2017 State Elimination: " + fileName + "...");
			long beginTime = System.nanoTime();
			Graph g = TTCStateCaseGraphLoader.load(taskMainPath + fileName); // get data
//			System.err.println(g);
			Graph result = algorithmStateCaseTTC2017.process(g).getOutput();
			long endTime = System.nanoTime();
//			System.err.println(result);
			System.out.println("Done after " + ((endTime - beginTime) / 1e9) + " seconds.");
			String resultStringRaw = "";
			for (String s: result.getNodes().get(0).getEdges().keySet()) {
				resultStringRaw = s;
			}
			String resultString = resultStringRaw.replaceAll(Pattern.quote("(ε)"), "");
			System.out.println(resultString + "\n");
		}
	}
	
	@Test
	public void testSolvingTTC2017StateEliminationCase() {
		for (String fileName: fileNamesTaskMain) {
			System.out.println("TTC2017 State Elimination: " + fileName + "...");
			long beginTime = System.nanoTime();
			Graph g = TTCStateCaseGraphLoader.load(taskMainPath + fileName); // get data
			Graph result = solveGraph(g); // solve problem
			long endTime = System.nanoTime();
			System.out.println("Done after " + ((endTime - beginTime) / 1e9) + " seconds.");
			String resultStringRaw = "";
			for (String s: result.getNodes().get(0).getEdges().keySet()) {
				resultStringRaw = s;
			}
			String resultString = resultStringRaw.replaceAll(Pattern.quote("(ε)"), "");
			System.out.println(resultString + "\n");
		}
	}
	
	@Test
	public void testSolvingTTC2017StateEliminationCaseExtension1() {
		for (String fileName: fileNamesTaskExtension1) {
			System.out.println("TTC2017 State Elimination (Extension 1): " + fileName + "...");
			long beginTime = System.nanoTime();
			Graph g = TTCStateCaseGraphLoader.load(taskExtension1Path + fileName); // get data
			Graph result = solveGraph(g); // solve problem
			long endTime = System.nanoTime();
			System.out.println("Done after " + ((endTime - beginTime) / 1e9) + " seconds.");
			String resultStringRaw = "";
			for (String s: result.getNodes().get(0).getEdges().keySet()) {
				resultStringRaw = s;
			}
			String resultString = resultStringRaw.replaceAll(Pattern.quote("(ε)"), "");
			System.out.println(resultString + "\n");
		}
		
	}
	
	private static Algorithm getStateCaseAlgorithmTTC2017() {
		
		Algorithm stateCaseTTC2017 = new Algorithm("# TTC 2017 State Case").setRepeating(false); // one time (each)
		
		Algorithm prepareData = new Algorithm("# prepare data").setRepeating(false); // one time (each)
		Algorithm eliminateState = new Algorithm("# eliminate state");
		Algorithm handleSourceNode = new Algorithm("# handle source node");
		Algorithm redirectRoute = new Algorithm("# redirect route");
		
		stateCaseTTC2017.addAlgorithmStep(prepareData);
			prepareData.addAlgorithmStep(getNewInitialPattern(), false); // one time (each)
			prepareData.addAlgorithmStep(getAddToInitialPattern());
			prepareData.addAlgorithmStep(getNewFinalPattern(), false); // one time (each)
			prepareData.addAlgorithmStep(getAddToFinalPattern());
			prepareData.addAlgorithmStep(getMergeEdgesPattern());
		stateCaseTTC2017.addAlgorithmStep(eliminateState);
			eliminateState.addAlgorithmStep(getMarkStateForEliminationPattern(), false); // one time (each)
			eliminateState.addAlgorithmStep(handleSourceNode);
				handleSourceNode.addAlgorithmStep(getMarkWithCurrentPattern(), false); // one time (each)
				handleSourceNode.addAlgorithmStep(getMarkFallbackWithCurrentPattern(), false); // one time (each)
				handleSourceNode.addAlgorithmStep(redirectRoute);
					redirectRoute.addAlgorithmStep(getPrepareStateWithPqPkKkKqPattern());
					redirectRoute.addAlgorithmStep(getPrepareStateWithPkKkKqPattern());
					redirectRoute.addAlgorithmStep(getPrepareStateWithPqPkKqPattern());
					redirectRoute.addAlgorithmStep(getPrepareStateWithPkKqPattern());
					redirectRoute.addAlgorithmStep(getPrepareStateWithPpPkKkKpPattern());
					redirectRoute.addAlgorithmStep(getPrepareStateWithPpPkKpPattern());
					redirectRoute.addAlgorithmStep(getPrepareStateWithPkKkKpPattern());
					redirectRoute.addAlgorithmStep(getPrepareStateWithPkKpPattern());
				handleSourceNode.addAlgorithmStep(getUnmarkCurrentPattern(), false); // one time (each)
				handleSourceNode.addAlgorithmStep(getRemoveMarksPattern());
			eliminateState.addAlgorithmStep(getEliminateMarkedStatePattern(), false); // one time (each)
			eliminateState.addAlgorithmStep(getUnmarkPastPattern(), false); // one time (each)
	
		return stateCaseTTC2017;
	}
	
	private Graph solveGraph(Graph g) {
		PatternGraph gtr_1_1 = getNewInitialPattern();
		PatternGraph gtr_1_1_b = getAddToInitialPattern();
		PatternGraph gtr_1_2 = getNewFinalPattern();
		PatternGraph gtr_1_3 = getAddToFinalPattern();
		PatternGraph gtr_1_4 = getMergeEdgesPattern();
		PatternGraph gtr_2_1 = getMarkStateForEliminationPattern();
		PatternGraph gtr_2_2_1 = getMarkWithCurrentPattern();
		PatternGraph gtr_2_2_2 = getMarkFallbackWithCurrentPattern();
		PatternGraph gtr_2_3_1 = getPrepareStateWithPqPkKkKqPattern();
		PatternGraph gtr_2_3_2 = getPrepareStateWithPkKkKqPattern();
		PatternGraph gtr_2_3_3 = getPrepareStateWithPqPkKqPattern();
		PatternGraph gtr_2_3_4 = getPrepareStateWithPkKqPattern();
		PatternGraph gtr_2_3_5 = getPrepareStateWithPpPkKkKpPattern();
		PatternGraph gtr_2_3_6 = getPrepareStateWithPpPkKpPattern();
		PatternGraph gtr_2_3_7 = getPrepareStateWithPkKkKpPattern();
		PatternGraph gtr_2_3_8 = getPrepareStateWithPkKpPattern();
		PatternGraph gtr_2_4 = getUnmarkCurrentPattern();
		PatternGraph gtr_2_5 = getRemoveMarksPattern();
		PatternGraph gtr_2_6 = getEliminateMarkedStatePattern();
		PatternGraph gtr_2_7 = getUnmarkPastPattern();
		
		/*
		 * How to loop:
		 * 
		 * #1.1
		 * #1.1 b)
		 * #1.2
		 * #1.3
		 * #1.4
		 *   #2.1       // mark k (as elimination)
		 *     #2.2.1   // mark p (as current)
		 *     #2.2.2   // mark p (as current)
		 *       #2.3.1 // mark q (as used)
		 *       #2.3.2 // mark q (as used)
		 *       #2.3.3 // mark q (as used)
		 *       #2.3.4 // mark q (as used)
		 *       #2.3.5 // mark q (as used)
		 *       #2.3.6 // mark q (as used)
		 *       #2.3.7 // mark q (as used)
		 *       #2.3.8 // mark q (as used)
		 *     #2.4     // mark p (as past)
		 *     #2.5     // unmark q
		 *   #2.6
		 *   #2.7
		 *  
		 */
		
		
		g = applyGTR(g, gtr_1_1, true);
		g = applyGTR(g, gtr_1_1_b);
		g = applyGTR(g, gtr_1_2, true);
		g = applyGTR(g, gtr_1_3);
		g = applyGTR(g, gtr_1_4);
		while (true) {
//			Graph copy1 = g.clone();
			Graph ref1 = g;
			g = applyGTR(g, gtr_2_1, true);
			while (true) {
//				Graph copy2 = g.clone();
				Graph ref2 = g;
				g = applyGTR(g, gtr_2_2_1, true);
				g = applyGTR(g, gtr_2_2_2, true);
				while (true) {
//					Graph copy3 = g.clone();
					Graph ref3 = g;
					g = applyGTR(g, gtr_2_3_1);
					g = applyGTR(g, gtr_2_3_2);
					g = applyGTR(g, gtr_2_3_3);
					g = applyGTR(g, gtr_2_3_4);
					g = applyGTR(g, gtr_2_3_5);
					g = applyGTR(g, gtr_2_3_6);
					g = applyGTR(g, gtr_2_3_7);
					g = applyGTR(g, gtr_2_3_8);
//					if (GraphEngine.isIsomorphTo(g, copy3)) {
					if (g == ref3) {
						break;
					}
				}
				g = applyGTR(g, gtr_2_4, true);
				g = applyGTR(g, gtr_2_5);
//				if (GraphEngine.isIsomorphTo(g, copy2)) {
				if (g == ref2) {
					break;
				}
			}
			g = applyGTR(g, gtr_2_6, true);
			g = applyGTR(g, gtr_2_7);
//			if (GraphEngine.isIsomorphTo(g, copy1)) {
			if (g == ref1) {
				break;
			}
		}
		return g;
	}
	
	@Test
	public void testLoadingTTCStateCaseData() {
		Graph g;
		/*
		 * just loading models and count nodes:
		 */
		String taskMainPath = "src/main/resources/ExperimentalData/testdata/emf/task-main/";

		String[] fileNames = {
				"leader3_2.xmi",
				"leader4_2.xmi",
				"leader3_3.xmi",
				"leader5_2.xmi",
				"leader3_4.xmi",
				"leader3_5.xmi",
				"leader4_3.xmi",
				"leader6_2.xmi",
				"leader3_6.xmi",
				"leader4_4.xmi",
				"leader5_3.xmi",
				"leader3_8.xmi",
				"leader4_5.xmi",
				"leader6_3.xmi",
				"leader4_6.xmi",
				"leader5_4.xmi",
				"leader5_5.xmi",
				"leader6_4.xmi",
				"leader6_5.xmi"
		};
		
		for (String fileName: fileNames) {
			g = TTCStateCaseGraphLoader.load(taskMainPath + fileName);
			System.out.println(fileName + ": " + g.getNodes().size());
		}
		
		g = TTCStateCaseGraphLoader.load(taskMainPath + "leader3_2.xmi");
		Assert.assertEquals(26, g.getNodes().size());
		
		g = TTCStateCaseGraphLoader.load(taskMainPath + "leader3_3.xmi");
		Assert.assertEquals(69, g.getNodes().size());
		
		g = TTCStateCaseGraphLoader.load(taskMainPath + "leader3_4.xmi");
		Assert.assertEquals(147, g.getNodes().size());
		
		g = TTCStateCaseGraphLoader.load(taskMainPath + "leader3_5.xmi");
		Assert.assertEquals(273, g.getNodes().size());
		
		g = TTCStateCaseGraphLoader.load(taskMainPath + "leader3_6.xmi");
		Assert.assertEquals(459, g.getNodes().size());
		
		g = TTCStateCaseGraphLoader.load(taskMainPath + "leader4_2.xmi");
		Assert.assertEquals(61, g.getNodes().size());
		
		g = TTCStateCaseGraphLoader.load(taskMainPath + "leader4_3.xmi");
		Assert.assertEquals(274, g.getNodes().size());
		
		g = TTCStateCaseGraphLoader.load(taskMainPath + "leader5_2.xmi");
		Assert.assertEquals(141, g.getNodes().size());
		
		g = TTCStateCaseGraphLoader.load(taskMainPath + "leader6_2.xmi");
		Assert.assertEquals(335, g.getNodes().size());
	}
	
	@Test
	public void testSomeIsolatedGTR() {
		
		PatternGraph gtr_1_1 = getNewInitialPattern();
		PatternGraph gtr_1_2 = getNewFinalPattern();
		PatternGraph gtr_1_3 = getAddToFinalPattern();
		PatternGraph gtr_1_4 = getMergeEdgesPattern();
//		PatternGraph gtr_2_1 = getMarkStateForEliminationPattern();
//		PatternGraph gtr_2_2 = getMarkWithCurrentPattern();
//		PatternGraph gtr_2_3_1 = getPrepareStateWithPqPkKkKqPattern();
		PatternGraph gtr_2_3_2 = getPrepareStateWithPkKkKqPattern();
//		PatternGraph gtr_2_3_3 = getPrepareStateWithPqPkKqPattern();
//		PatternGraph gtr_2_3_4 = getPrepareStateWithPkKqPattern();
//		PatternGraph gtr_2_4 = getUnmarkCurrentPattern();
//		PatternGraph gtr_2_5 = getRemoveMarksPattern();
//		PatternGraph gtr_2_6 = getEliminateMarkedStatePattern();
//		PatternGraph gtr_2_7 = getUnmarkPastPattern();
		
		ArrayList<Match> matches;
		Graph result;
		
		Graph g_1_1 = new Graph();
		Node n_1_1 = new Node().setAttribute("initial", true);
		g_1_1.addNode(n_1_1);
		matches = PatternEngine.matchPattern(g_1_1, gtr_1_1, true);
		Assert.assertEquals(1, matches.size());
		result = PatternEngine.applyMatch(matches.get(0));
		Assert.assertTrue(!GraphEngine.isIsomorphTo(g_1_1, result));
		
		Graph g_1_2 = new Graph();
		Node n_1_2_a = new Node().setAttribute("final", true);
		Node n_1_2_b = new Node().setAttribute("final", true);
		g_1_2.addNode(n_1_2_a, n_1_2_b);
		matches = PatternEngine.matchPattern(g_1_2, gtr_1_2, false);
		Assert.assertEquals(2, matches.size());
		matches = PatternEngine.matchPattern(g_1_2, gtr_1_2, true);
		Assert.assertEquals(1, matches.size());
		result = PatternEngine.applyMatch(matches.get(0));
		Assert.assertTrue(!GraphEngine.isIsomorphTo(g_1_2, result));
		g_1_2 = result;
		matches = PatternEngine.matchPattern(g_1_2, gtr_1_2, false);
		Assert.assertEquals(0, matches.size());
		
		Graph g_1_3 = g_1_2;
		matches = PatternEngine.matchPattern(g_1_3, gtr_1_3, false);
		Assert.assertEquals(1, matches.size());
		result = PatternEngine.applyMatch(matches.get(0));
		Assert.assertTrue(!GraphEngine.isIsomorphTo(g_1_3, result));
		
		Graph g_1_4 = new Graph();
		Node n_1_4_a = new Node();
		Node n_1_4_b = new Node();
		g_1_4.addNode(n_1_4_a, n_1_4_b);
		n_1_4_a.addEdge("a", n_1_4_b);
		n_1_4_a.addEdge("b", n_1_4_b);
		n_1_4_a.addEdge("c", n_1_4_b);
		matches = PatternEngine.matchPattern(g_1_4, gtr_1_4, false);
		Assert.assertEquals(1, matches.size());
		result = PatternEngine.applyMatch(matches.get(0));
		Assert.assertTrue(!GraphEngine.isIsomorphTo(g_1_4, result));
		g_1_4 = result;
		matches = PatternEngine.matchPattern(g_1_4, gtr_1_4, false);
		Assert.assertEquals(1, matches.size());
		result = PatternEngine.applyMatch(matches.get(0));

		Graph g_2_3_2 = new Graph();
		Node n_2_3_2_p = new Node().setAttribute("current", true);
		Node n_2_3_2_k = new Node().setAttribute("eliminate", true);
		Node n_2_3_2_q = new Node();
		g_2_3_2.addNode(n_2_3_2_p, n_2_3_2_k, n_2_3_2_q);
		n_2_3_2_p.addEdge("a", n_2_3_2_k);
		n_2_3_2_k.addEdge("b", n_2_3_2_k);
		n_2_3_2_k.addEdge("c", n_2_3_2_q);
		matches = PatternEngine.matchPattern(g_2_3_2, gtr_2_3_2, false);
		Assert.assertEquals(1, matches.size());
		result = PatternEngine.applyMatch(matches.get(0));
		System.out.println(g_2_3_2);
		System.out.println(result);
		// TODO
		
	}
	
	@Test
	public void testSomeProblematicGTR() {
		String gtrString = "{\"nodes\":[{\"id\":0,\"attributes\":{\"current\":true},\"edges\":[{\"((((((((s2)(s10))(s18))(s25))+((((s6)(s14))(s20))(s25)))+((((s5)(s13))(s23))(s25)))+((((s4)(s15))(s24))(s25)))+((((s7)(s12))(s21))(s25)))+((((s3)(s16))(s19))(s25))\":[1]},{\"((s8)(s11))(s22)\":[2]}]},{\"id\":2,\"attributes\":{\"eliminate\":true},\"edges\":[{\"s0\":[0]}]},{\"id\":1,\"attributes\":{},\"edges\":[{\"s25\":[1]},{\"ε\":[3]}]},{\"id\":4,\"attributes\":{\"newInitial\":true},\"edges\":[{\"ε\":[0]}]},{\"id\":3,\"attributes\":{\"newFinal\":true},\"edges\":[]}]}";
		Graph g = GraphEngine.getGson().fromJson(gtrString, Graph.class);
		PatternGraph gtr = getPrepareStateWithPkKpPattern();
		
		ArrayList<Match> matches = PatternEngine.matchPattern(g, gtr, true);
		Assert.assertTrue(matches.size() > 0);
		
		Graph result = applyGTR(g, gtr);
		Assert.assertTrue(!GraphEngine.isIsomorphTo(g, result));
	}
	
	Graph applyGTR(Graph g, PatternGraph gtr) {
		return applyGTR(g, gtr, false);
	}

	Graph applyGTR(Graph g, PatternGraph gtr, boolean single) {
		boolean foundOne = false;
		do {
			foundOne = false;
			ArrayList<Match> matches = PatternEngine.matchPattern(g, gtr, true);
			if (matches.size() > 0) {
				foundOne = true;
				g = PatternEngine.applyMatch(matches.get(0));
//				System.out.println(gtr.getName() + ":\n" + g + "\n");
			}
		} while (!single && foundOne);
		return g;
	}
	
	private static PatternGraph getNewInitialPattern() { // #1.1 (single match; don't repeat) - could also be repeated
		// gtr for new initial state:
		PatternGraph gtr = new PatternGraph("new initial state");
		PatternNode initialNode = new PatternNode("#{initial}").addPatternAttribute(new PatternAttribute().setAction("-").setName("initial"));
		PatternNode newInitialNode = new PatternNode().setAction("+").addPatternAttribute(new PatternAttribute().setAction("+").setName("newInitial").setValue(true));
		PatternNode noExistingNewInitialNode = new PatternNode("#{newInitial}").setAction("!=");
		gtr.addPatternNode(initialNode, newInitialNode, noExistingNewInitialNode);
		newInitialNode.addPatternEdge("+", "ε", initialNode);
		return gtr;
	}

	private static PatternGraph getAddToInitialPattern() { // #1.1b (single match; do repeat)
		// gtr for adding to new initial state:
		PatternGraph gtr = new PatternGraph("adding to the existing new initial state");
		PatternNode otherInitialNode = new PatternNode("#{initial}").addPatternAttribute(new PatternAttribute().setAction("-").setName("initial"));
		PatternNode existingNewInitialNode = new PatternNode("#{newInitial}");
		gtr.addPatternNode(otherInitialNode, existingNewInitialNode);
		existingNewInitialNode.addPatternEdge("+", "ε", otherInitialNode);
		return gtr;
	}

	private static PatternGraph getNewFinalPattern() { // #1.2 (single match; don't repeat) - could also be repeated
		// gtr for new final state:
		PatternGraph gtr = new PatternGraph("new final state");
		PatternNode finalNode = new PatternNode("#{final}").addPatternAttribute(new PatternAttribute().setAction("-").setName("final"));
		PatternNode newFinalNode = new PatternNode().setAction("+").addPatternAttribute(new PatternAttribute().setAction("+").setName("newFinal").setValue(true));
		PatternNode noExistingNewFinalNode = new PatternNode("#{newFinal}").setAction("!=");
		gtr.addPatternNode(finalNode, newFinalNode, noExistingNewFinalNode);
		finalNode.addPatternEdge("+", "ε", newFinalNode);
		return gtr;
	}

	private static PatternGraph getAddToFinalPattern() { // #1.3 (single match; do repeat)
		// gtr for adding to new final state:
		PatternGraph gtr = new PatternGraph("adding to the existing new final state");
		PatternNode otherFinalNode = new PatternNode("#{final}").addPatternAttribute(new PatternAttribute().setAction("-").setName("final"));
		PatternNode existingNewFinalNode = new PatternNode("#{newFinal}");
		gtr.addPatternNode(otherFinalNode, existingNewFinalNode);
		otherFinalNode.addPatternEdge("+", "ε", existingNewFinalNode);
		return gtr;
	}
	
	private static PatternGraph getMergeEdgesPattern() { // #1.4 (single match; do repeat)
		// gtr for merging multiple edges between the same two nodes:
		PatternGraph gtr = new PatternGraph("merge multiple labels between the same two nodes");
		PatternNode a = new PatternNode();
		PatternNode b = new PatternNode();
		gtr.addPatternNode(a, b);
		a.addPatternEdge("-", "#{x}, #{y}", b);
		a.addPatternEdge("+", "#{x} + '+' + #{y}", b);
		return gtr;
	}
	
	private static PatternGraph getMarkStateForEliminationPattern() { // #2.1 (single match; don't repeat) - could also be repeated
		// gtr for marking a state for elimination
		PatternGraph gtr = new PatternGraph("mark state for elimination");
		PatternNode p = new PatternNode();
		PatternNode k = new PatternNode("!(#{newFinal} || #{newInitial} || #{eliminate})");
		PatternNode q = new PatternNode();
		PatternNode noOtherMarkedOne = new PatternNode("#{eliminate}").setAction("!=");
		k.addPatternAttribute(new PatternAttribute().setAction("+").setName("eliminate").setValue(true));
		gtr.addPatternNode(p, k, q);
		gtr.addPatternNode(noOtherMarkedOne);
		return gtr;
	}

	private static PatternGraph getMarkWithCurrentPattern() { // #2.2.1
		// gtr for marking the current state for elimination preparation
		PatternGraph gtr = new PatternGraph("mark current working state (p->k->q)");
		PatternNode noAlreadyMarkedAsCurrentState = new PatternNode("#{current}").setAction("!=");
		PatternNode p = new PatternNode("!(#{current}) && !(#{past})").addPatternAttribute(new PatternAttribute().setAction("+").setName("current").setValue(true));
		PatternNode k = new PatternNode("#{eliminate} == 1");
		PatternNode q = new PatternNode();
		gtr.addPatternNode(noAlreadyMarkedAsCurrentState, p, k, q);
		p.addPatternEdge("==", "#{a}", k);
		k.addPatternEdge("==", "#{b}", q);
		return gtr;
	}

	private static PatternGraph getMarkFallbackWithCurrentPattern() { // #2.2.1
		// gtr for marking the current state for elimination preparation
		PatternGraph gtr = new PatternGraph("mark current working state (p<->k)");
		PatternNode noAlreadyMarkedAsCurrentState = new PatternNode("#{current}").setAction("!=");
		PatternNode p = new PatternNode("!(#{current}) && !(#{past})").addPatternAttribute(new PatternAttribute().setAction("+").setName("current").setValue(true));
		PatternNode k = new PatternNode("#{eliminate} == 1");
		gtr.addPatternNode(noAlreadyMarkedAsCurrentState, p, k);
		p.addPatternEdge("==", "#{a}", k);
		k.addPatternEdge("==", "#{b}", p);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPqPkKkKqPattern() { // #2.3.1 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with pq, pk, kk, kq)");
		PatternNode p = new PatternNode("#{current}");
		PatternNode k = new PatternNode("#{eliminate}");
		PatternNode q = new PatternNode("!(#{used})").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		gtr.addPatternNode(p, k, q);
		/* CASE 1: there's pq, pk, kk and kq */
		p.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("==", "#{kq}", q);
		k.addPatternEdge("==", "#{kk}", k);
		p.addPatternEdge("-", "#{pq}", q);
		p.addPatternEdge("+", "'(' + #{pq} + ')+((' + #{pk} + ')(' + #{kk} + ')*(' + #{kq} + '))'", q);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPkKkKqPattern() { // #2.3.2 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with just pk, kk, kq)");
		PatternNode p = new PatternNode("#{current}");
		PatternNode k = new PatternNode("#{eliminate}");
		PatternNode q = new PatternNode("!(#{used})").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		gtr.addPatternNode(p, k, q);
		/* CASE 2: there's just pk, kk and kq */
		p.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("==", "#{kk}", k);
		k.addPatternEdge("==", "#{kq}", q);
		p.addPatternEdge("+", "'(' + #{pk} + ')(' + #{kk} + ')*(' + #{kq} + ')'", q);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPqPkKqPattern() { // #2.3.3 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with just pq, pk, kq)");
		PatternNode p = new PatternNode("#{current}");
		PatternNode k = new PatternNode("#{eliminate}");
		PatternNode q = new PatternNode("!(#{used})").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		gtr.addPatternNode(p, k, q);
		/* CASE 3: there's just pq, pk and kq */
		p.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("==", "#{kq}", q);
		p.addPatternEdge("-", "#{pq}", q);
		p.addPatternEdge("+", "'(' + #{pq} + ')+((' + #{pk} + ')(' + #{kq} + '))'", q);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPkKqPattern() { // #2.3.4 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with just pk, kq)");
		PatternNode p = new PatternNode("#{current}");
		PatternNode k = new PatternNode("#{eliminate}");
		PatternNode q = new PatternNode("!(#{used})").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		gtr.addPatternNode(p, k, q);
		/* CASE 4: there's just pk and kq */
		p.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("==", "#{kq}", q);
		p.addPatternEdge("+", "'(' + #{pk} + ')(' + #{kq} + ')'", q);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPpPkKkKpPattern() { // #2.3.5 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with pp, pk, kk, kp)");
		PatternNode p = new PatternNode("#{current} && !(#{used})").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		PatternNode k = new PatternNode("#{eliminate}");
		gtr.addPatternNode(p, k);
		/* CASE 5: there's pp, pk, kk and kp */
		p.addPatternEdge("-", "#{pp}", p);
		p.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("==", "#{kk}", k);
		k.addPatternEdge("==", "#{kp}", p);
		p.addPatternEdge("+", "'((' + #{pp} + ')*((' + #{pk} + ')(' + #{kk} + ')*(' + #{kp} + '))*)*'", p);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPpPkKpPattern() { // #2.3.6 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with just pp, pk, kp)");
		PatternNode p = new PatternNode("#{current} && !(#{used})").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		PatternNode k = new PatternNode("#{eliminate}");
		gtr.addPatternNode(p, k);
		/* CASE 5: there's pp, pk, kk and kp */
		p.addPatternEdge("-", "#{pp}", p);
		p.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("==", "#{kp}", p);
		p.addPatternEdge("+", "'((' + #{pp} + ')*((' + #{pk} + ')(' + #{kp} + '))*)*'", p);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPkKkKpPattern() { // #2.3.7 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with just pk, kk, kp)");
		PatternNode p = new PatternNode("#{current} && !(#{used})").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		PatternNode k = new PatternNode("#{eliminate}");
		gtr.addPatternNode(p, k);
		/* CASE 5: there's pp, pk, kk and kp */
		p.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("==", "#{kk}", k);
		k.addPatternEdge("==", "#{kp}", p);
		p.addPatternEdge("+", "'((' + #{pk} + ')(' + #{kk} + ')*(' + #{kp} + '))*'", p);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPkKpPattern() { // #2.3.8 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with just pk, kp)");
		PatternNode p = new PatternNode("#{current} && !(#{used})").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		PatternNode k = new PatternNode("#{eliminate}");
		gtr.addPatternNode(p, k);
		/* CASE 5: there's pp, pk, kk and kp */
		p.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("==", "#{kp}", p);
		p.addPatternEdge("+", "'((' + #{pk} + ')(' + #{kp} + '))*'", p);
		return gtr;
	}

	private static PatternGraph getUnmarkCurrentPattern() { // #2.4
		PatternGraph gtr = new PatternGraph("remove mark of current working state");
		PatternNode n = new PatternNode("#{current}");
		n.addPatternAttribute(new PatternAttribute().setAction("-").setName("current"));
		n.addPatternAttribute(new PatternAttribute().setAction("+").setName("past").setValue(true));
		gtr.addPatternNode(n);
		return gtr;
	}
	
	private static PatternGraph getRemoveMarksPattern() { // #2.5 (all matches; don't repeat) - could also be repeated
		// gtr for removing marks
		PatternGraph gtr = new PatternGraph("remove mark of used state");
		PatternNode n = new PatternNode("#{used}").addPatternAttribute(new PatternAttribute().setAction("-").setName("used"));
		gtr.addPatternNode(n);
		return gtr;
	}
	
	private static PatternGraph getEliminateMarkedStatePattern() { // #2.6 (single match; don't repeat) - could also be repeated
		// gtr for eliminating the previously marked state
		PatternGraph gtr = new PatternGraph("eliminate state itself");
		PatternNode k = new PatternNode("#{eliminate}").setAction("-");
		gtr.addPatternNode(k);
		return gtr;
	}
	
	private static PatternGraph getUnmarkPastPattern() { // #2.7
		PatternGraph gtr = new PatternGraph("remove mark of past working state");
		PatternNode n = new PatternNode("#{past}");
		n.addPatternAttribute(new PatternAttribute().setAction("-").setName("past"));
		gtr.addPatternNode(n);
		return gtr;
	}
	
}