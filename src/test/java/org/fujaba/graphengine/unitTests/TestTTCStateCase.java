package org.fujaba.graphengine.unitTests;

import java.util.ArrayList;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.Match;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.pattern.PatternAttribute;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;
import org.fujaba.graphengine.stateelimination.TTCStateCaseGraphLoader;
import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.jeval.EvaluationException;

public class TestTTCStateCase {
	
	@Test
	public void testLoadingTTCStateCaseData() {
		String taskMainPath = "src/main/resources/ExperimentalData/testdata/emf/task-main/";
		Graph g;
		
		/*
		 * just loading models and count nodes:
		 */
		
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
		// TODO
		
	}
	
	@Test
	public void testUsingGTR() {
		PatternGraph gtr_1_1 = getNewInitialPattern();
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

		// get data:
		String taskMainPath = "src/main/resources/ExperimentalData/testdata/emf/task-main/";
		Graph g = TTCStateCaseGraphLoader.load(taskMainPath + "leader3_2.xmi");
		System.out.println("loaded:\n" + g + "\n");
		
		
		g = applyGTR(g, gtr_1_1, true);
		g = applyGTR(g, gtr_1_2, true);
		g = applyGTR(g, gtr_1_3);
		g = applyGTR(g, gtr_1_4);
		System.out.println("after 1.4:\n" + g + "\n");
		while (true) {
			Graph copy1 = g.clone();
			g = applyGTR(g, gtr_2_1, true);
			System.out.println("after 2.1:\n" + g + "\n");
			while (true) {
				Graph copy2 = g.clone();
				g = applyGTR(g, gtr_2_2_1, true);
				System.out.println("after 2.2.1:\n" + g + "\n");
				g = applyGTR(g, gtr_2_2_2, true);
				System.out.println("after 2.2.2:\n" + g + "\n");
				while (true) {
					Graph copy3 = g.clone();
					
					

					g = applyGTR(g, gtr_2_3_5);
					System.out.println("after 2.3.5:\n" + g + "\n");
					g = applyGTR(g, gtr_2_3_6);
					System.out.println("after 2.3.6:\n" + g + "\n");
					g = applyGTR(g, gtr_2_3_7);
					System.out.println("after 2.3.7:\n" + g + "\n");
					g = applyGTR(g, gtr_2_3_8);
					System.out.println("after 2.3.8:\n" + g + "\n");
					
					
					
					g = applyGTR(g, gtr_2_3_1);
					System.out.println("after 2.3.1:\n" + g + "\n");
					g = applyGTR(g, gtr_2_3_2);
					System.out.println("after 2.3.2:\n" + g + "\n");
					g = applyGTR(g, gtr_2_3_3);
					System.out.println("after 2.3.3:\n" + g + "\n");
					g = applyGTR(g, gtr_2_3_4);
					System.out.println("after 2.3.4:\n" + g + "\n");
					
					
					
					if (GraphEngine.isIsomorphTo(g, copy3)) {
						break;
					}
				}
				g = applyGTR(g, gtr_2_4, true);
				System.out.println("after 2.4:\n" + g + "\n");
				g = applyGTR(g, gtr_2_5);
				System.out.println("after 2.5:\n" + g + "\n");
				if (GraphEngine.isIsomorphTo(g, copy2)) {
					break;
				}
			}
			g = applyGTR(g, gtr_2_6, true);
			System.out.println("after 2.6:\n" + g + "\n");
			g = applyGTR(g, gtr_2_7);
			System.out.println("after 2.7:\n" + g + "\n");
			if (GraphEngine.isIsomorphTo(g, copy1)) {
				break;
			}
		}
	}
	
	Graph applyGTR(Graph g, PatternGraph gtr) {
		return applyGTR(g, gtr, false);
	}

	Graph applyGTR(Graph g, PatternGraph gtr, boolean single) {
		boolean foundOne = false;
		do {
			foundOne = false;
//			System.out.println("matching GTR: " + gtr.getName());
			ArrayList<Match> matches = PatternEngine.matchPattern(g, gtr, true);
//			System.out.println("found: " + matches.size());
			if (matches.size() > 0) {
				foundOne = true;
				g = PatternEngine.applyMatch(matches.get(0));
			}
		} while (!single && foundOne);
		return g;
	}
	
	
//	@Test
//	public void testTransformingTTCStateCaseData() {
//
//		// get data:
//		String taskMainPath = "src/main/resources/ExperimentalData/testdata/emf/task-main/";
//		Graph g = TTCStateCaseGraphLoader.load(taskMainPath + "leader3_2.xmi");
//		System.out.println("loaded:\n" + g + "\n");
//		
//		// gtr for new initial state:
//		PatternGraph gtrInitial = getNewInitialPattern();
//		ArrayList<Match> matches = PatternEngine.matchPattern(g, gtrInitial, true); // just a single match
//		Assert.assertEquals(1, matches.size());
//		g = PatternEngine.applyMatch(matches.get(0));
//		System.out.println("added new initial state (and removed 'initial' flag for the old one):\n" + g + "\n");
//		
//		// gtr for new final state:
//		PatternGraph gtrFinal = getNewFinalPattern();
//		matches = PatternEngine.matchPattern(g, gtrFinal, true); // just a single match
//		Assert.assertEquals(1, matches.size());
//		g = PatternEngine.applyMatch(matches.get(0));
//		System.out.println("added new final state (and removed 'final' flag for the old one):\n" + g + "\n");
//		
//		PatternGraph gtrOtherFinal = getAddToFinalPattern();
//		while (true) {
//			matches = PatternEngine.matchPattern(g, gtrOtherFinal, true); // just a single match (each time)
//			if (matches != null && matches.size() > 0) {
//				g = PatternEngine.applyMatch(matches.get(0));
//				System.out.println("added edge to the new final state (and removed 'final' flag for the other one):\n" + g + "\n");
//			} else {
//				break;
//			}
//		}
//
//		PatternGraph gtrMerge = getMergeEdgesPattern();
//		while (true) {
//			matches = PatternEngine.matchPattern(g, gtrMerge, true); // just a single match (each time)
//			if (matches != null && matches.size() > 0) {
//				g = PatternEngine.applyMatch(matches.get(0));
//				System.out.println("merged label:\n" + g + "\n");
//			} else {
//				break;
//			}
//		}
//		
//		
//		// gtr for joining multiple edges between the same two nodes
////		/**
////		 * this doesn't seem possible right now. you can't have a PatternGraph that says:
////		 * 'find two (different) edges with any label between some nodes!'
////		 * 
////		 * so we unfortunately seem to have to do this by hand:
////		 */
////		ArrayList<Node> sources = new ArrayList<Node>();
////		ArrayList<Node> targets = new ArrayList<Node>();
////		ArrayList<String> firstLabels = new ArrayList<String>();
////		ArrayList<String> secondLabels = new ArrayList<String>();
////		for (Node source: g.getNodes()) {
////			for (Node target: g.getNodes()) {
////				for (String sourceLabel1: source.getEdges().keySet()) {
////					if (source.getEdges(sourceLabel1).contains(target)) {
////						for (String sourceLabel2: source.getEdges().keySet()) {
////							if (!sourceLabel1.equals(sourceLabel2) && source.getEdges(sourceLabel2).contains(target)) {
////								sources.add(source);
////								targets.add(target);
////								firstLabels.add(sourceLabel1);
////								secondLabels.add(sourceLabel2);
////							}
////						}
////					}
////				}
////			}
////		}
////		for (int i = 0; i < sources.size(); ++i) {
////			sources.get(i).removeEdge(firstLabels.get(i), targets.get(i));
////			sources.get(i).removeEdge(secondLabels.get(i), targets.get(i));
////			if (firstLabels.get(i) != null && firstLabels.get(i) != "" && firstLabels.get(i) != "this_is_no_real_edge" && secondLabels.get(i) != null && secondLabels.get(i) != "" && secondLabels.get(i) != "this_is_no_real_edge") {
////				sources.get(i).addEdge("(" + firstLabels.get(i) + ")(" + secondLabels.get(i) + ")]", targets.get(i));
////			} else if (firstLabels.get(i) != null && firstLabels.get(i) != "" && firstLabels.get(i) != "this_is_no_real_edge") {
////				sources.get(i).addEdge(firstLabels.get(i), targets.get(i));
////			} else if (secondLabels.get(i) != null && secondLabels.get(i) != "" && secondLabels.get(i) != "this_is_no_real_edge") {
////				sources.get(i).addEdge(secondLabels.get(i), targets.get(i));
////			}
////		}
//		System.out.println("after joining multiple labels with the same sources and same targets:\n" + g + "\n");
//		
//		
//		
//		
//		/**
//		 * TODO: really just randomly find a 'k' and then find all matching 'p's and 'q's for it, to work with them in a 'batch'
//		 */
//		
//		
//		// gtr for adding new calculated labels
//		PatternGraph gtrEliminate1 = getEliminateStateWithPqPkKkKqPattern();
//		PatternGraph gtrEliminate2 = getEliminateStateWithPkKkKqPattern();
//		PatternGraph gtrEliminate3 = getEliminateStateWithPqPkKqPattern();
//		PatternGraph gtrEliminate4 = getEliminateStateWithPkKqPattern();
//		
//		do {
//			matches = PatternEngine.matchPattern(g, gtrEliminate, true);
//			if (matches != null && matches.size() > 0) {
//				Match match = matches.get(0);
//				
//				try {
//					String toEvaluate = "'(' + #{pk} + ')(' + #{kq} + ')'";
//					String test = match.getLabelEvaluator().evaluate("'' + (" + toEvaluate + ")");
//					test = test.substring(1, test.length() - 1);
//					System.err.println(test);
//				} catch (EvaluationException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				// do even more things by hand :(
//				Node pNode = match.getNodeMatch().get(p);
//				Node kNode = match.getNodeMatch().get(k);
//				Node qNode = match.getNodeMatch().get(q);
//				// there's at most one edge between any two nodes;
//				String pqLabel = "";
//				for (String s: pNode.getEdges().keySet()) {
//					if (pNode.getEdges(s).contains(qNode)) {
//						pqLabel = s;
//					}
//				}
//				Boolean pqb = false;
//				if (pqLabel != null && pqLabel != "") {
//					pqb = true;
//				}
////				System.out.println("    pqLabel: " + pqLabel);
//				String pkLabel = ""; 
//				for (String s: pNode.getEdges().keySet()) {
//					if (pNode.getEdges(s).contains(kNode)) {
//						pkLabel = s;
//					}
//				}
////				System.out.println("(+) pkLabel: " + pkLabel);
//				String kkLabel = "";
//				for (String s: kNode.getEdges().keySet()) {
//					if (kNode.getEdges(s).contains(kNode)) {
//						kkLabel = s;
//					}
//				}
//				Boolean kkb = false;
//				if (kkLabel != null && kkLabel != "") {
//					kkb = true;
//				}
////				System.out.println("    kkLabel: " + kkLabel);
//				String kqLabel = "";
//				for (String s: kNode.getEdges().keySet()) {
//					if (kNode.getEdges(s).contains(qNode)) {
//						kqLabel = s;
//					}
//				}
////				System.out.println("(+) kqLabel: " + kqLabel);
//				g.removeNode(kNode);
//				pNode.removeEdge(pqLabel, qNode);
//				pNode.addEdge((pqb ? "[(" + pqLabel + ")" : "") + "(" + pkLabel + ")" + (kkb ? "(" + kkLabel + ")*" : "") + "(" + kqLabel + ")" + (pqb ? "]" : ""), qNode);
//				System.out.println("eliminated a state (q->k->p to q->p):\n" + g + "\n");
//			} else {
//				break;
//			}
//		} while (true);
//		
//		System.out.println("final graph: " + g);
//		
//	}
	
	private static PatternGraph getNewInitialPattern() { // #1.1 (single match; don't repeat) - could also be repeated
		// gtr for new initial state:
		PatternGraph gtr = new PatternGraph("new initial state");
		PatternNode initialNode = new PatternNode("#{initial} == 1").addPatternAttribute(new PatternAttribute().setAction("-").setName("initial"));
		PatternNode newInitialNode = new PatternNode().setAction("+").addPatternAttribute(new PatternAttribute().setAction("+").setName("newInitial").setValue(true));
		PatternNode noExistingNewInitialNode = new PatternNode("#{newInitial} == 1").setAction("!=");
		gtr.addPatternNode(initialNode, newInitialNode, noExistingNewInitialNode);
		newInitialNode.addPatternEdge("+", "ε", initialNode);
		return gtr;
	}

	private static PatternGraph getNewFinalPattern() { // #1.2 (single match; don't repeat) - could also be repeated
		// gtr for new final state:
		PatternGraph gtr = new PatternGraph("new final state");
		PatternNode finalNode = new PatternNode("#{final} == 1").addPatternAttribute(new PatternAttribute().setAction("-").setName("final"));
		PatternNode newFinalNode = new PatternNode().setAction("+").addPatternAttribute(new PatternAttribute().setAction("+").setName("newFinal").setValue(true));
		PatternNode noExistingNewFinalNode = new PatternNode("#{newFinal} == 1").setAction("!=");
		gtr.addPatternNode(finalNode, newFinalNode, noExistingNewFinalNode);
		finalNode.addPatternEdge("+", "ε", newFinalNode);
		return gtr;
	}

	private static PatternGraph getAddToFinalPattern() { // #1.3 (single match; do repeat)
		// gtr for adding to new final state:
		PatternGraph gtr = new PatternGraph("adding to the existing new final state");
		PatternNode otherFinalNode = new PatternNode("#{final} == 1").addPatternAttribute(new PatternAttribute().setAction("-").setName("final"));
		PatternNode existingNewFinalNode = new PatternNode("#{newFinal} == 1");
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
		PatternNode k = new PatternNode("!(#{newFinal} == 1 || #{newInitial} == 1 || #{eliminate} == 1)");
		PatternNode q = new PatternNode();
		PatternNode noOtherMarkedOne = new PatternNode("#{eliminate} == 1").setAction("!="); // with this, it can also be repeated
		k.addPatternAttribute(new PatternAttribute().setAction("+").setName("eliminate").setValue(true));
		gtr.addPatternNode(p, k, q);
		gtr.addPatternNode(noOtherMarkedOne); // with this, it can also be repeated
		return gtr;
	}

	private static PatternGraph getMarkWithCurrentPattern() { // #2.2.1
		// gtr for marking the current state for elimination preparation
		PatternGraph gtr = new PatternGraph("mark current working state");
		PatternNode noAlreadyMarkedAsCurrentState = new PatternNode("#{current} == 1").setAction("!=");
		PatternNode p = new PatternNode("#{current} != 1 && #{past} != 1").addPatternAttribute(new PatternAttribute().setAction("+").setName("current").setValue(true));
		PatternNode k = new PatternNode("#{eliminate} == 1");
		PatternNode q = new PatternNode();
		gtr.addPatternNode(noAlreadyMarkedAsCurrentState, p, k, q);
		p.addPatternEdge("==", "#{a}", k);
		k.addPatternEdge("==", "#{b}", q);
		return gtr;
	}

	private static PatternGraph getMarkFallbackWithCurrentPattern() { // #2.2.1
		// gtr for marking the current state for elimination preparation
		PatternGraph gtr = new PatternGraph("mark current working state");
		PatternNode noAlreadyMarkedAsCurrentState = new PatternNode("#{current} == 1").setAction("!=");
		PatternNode p = new PatternNode("#{current} != 1 && #{past} != 1").addPatternAttribute(new PatternAttribute().setAction("+").setName("current").setValue(true));
		PatternNode k = new PatternNode("#{eliminate} == 1");
		gtr.addPatternNode(noAlreadyMarkedAsCurrentState, p, k);
		p.addPatternEdge("==", "#{a}", k);
		k.addPatternEdge("==", "#{b}", p);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPqPkKkKqPattern() { // #2.3.1 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with pq, pk, kk, kq)");
		PatternNode p = new PatternNode("#{current} == 1");
		PatternNode k = new PatternNode("#{eliminate} == 1");
		PatternNode q = new PatternNode("#{used} != 1").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		gtr.addPatternNode(p, k, q);
		/* CASE 1: there's pq, pk, kk and kq */
		p.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("==", "#{kq}", q);
		k.addPatternEdge("==", "#{kk}", k);
		p.addPatternEdge("-", "#{pq}", q);
		p.addPatternEdge("+", "'(' + #{pq} + ')+(' + #{pk} + ')(' + #{kk} + ')*(' + #{kq} + ')'", q);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPkKkKqPattern() { // #2.3.2 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with just pk, kk, kq)");
		PatternNode p = new PatternNode("#{current} == 1");
		PatternNode k = new PatternNode("#{eliminate} == 1");
		PatternNode q = new PatternNode("#{used} != 1").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		gtr.addPatternNode(p, k, q);
		/* CASE 2: there's just pk, kk and kq */
		p.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("==", "#{kq}", q);
		k.addPatternEdge("==", "#{kk}", k);
		p.addPatternEdge("+", "'(' + #{pk} + ')(' + #{kk} + ')*(' + #{kq} + ')'", q);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPqPkKqPattern() { // #2.3.3 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with just pq, pk, kq)");
		PatternNode p = new PatternNode("#{current} == 1");
		PatternNode k = new PatternNode("#{eliminate} == 1");
		PatternNode q = new PatternNode("#{used} != 1").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		gtr.addPatternNode(p, k, q);
		/* CASE 3: there's just pq, pk and kq */
		p.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("==", "#{kq}", q);
		p.addPatternEdge("-", "#{pq}", q);
		p.addPatternEdge("+", "'(' + #{pq} + ')+(' + #{pk} + ')(' + #{kq} + ')'", q);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPkKqPattern() { // #2.3.4 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with just pk, kq)");
		PatternNode p = new PatternNode("#{current} == 1");
		PatternNode k = new PatternNode("#{eliminate} == 1");
		PatternNode q = new PatternNode("#{used} != 1").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
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
		PatternNode p = new PatternNode("#{current} == 1 && #{used} != 1").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		PatternNode k = new PatternNode("#{eliminate} == 1");
		gtr.addPatternNode(p, k);
		/* CASE 5: there's pp, pk, kk and kp */
		p.addPatternEdge("==", "#{pp}", p);
		k.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("==", "#{kk}", k);
		k.addPatternEdge("-", "#{kp}", p);
		p.addPatternEdge("+", "'(' + #{pp} + ')+(' + #{pk} + ')(' + #{kk} + ')*(' + #{kp} + ')'", p);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPpPkKpPattern() { // #2.3.6 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with just pp, pk, kp)");
		PatternNode p = new PatternNode("#{current} == 1 && #{used} != 1").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		PatternNode k = new PatternNode("#{eliminate} == 1");
		gtr.addPatternNode(p, k);
		/* CASE 5: there's pp, pk, kk and kp */
		p.addPatternEdge("==", "#{pp}", p);
		k.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("-", "#{kp}", p);
		p.addPatternEdge("+", "'(' + #{pp} + ')+(' + #{pk} + ')(' + #{kp} + ')'", p);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPkKkKpPattern() { // #2.3.7 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with just pk, kk, kp)");
		PatternNode p = new PatternNode("#{current} == 1 && #{used} != 1").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		PatternNode k = new PatternNode("#{eliminate} == 1");
		gtr.addPatternNode(p, k);
		/* CASE 5: there's pp, pk, kk and kp */
		k.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("==", "#{kk}", k);
		k.addPatternEdge("-", "#{kp}", p);
		p.addPatternEdge("+", "'(' + #{pk} + ')(' + #{kk} + ')*(' + #{kp} + ')'", p);
		return gtr;
	}

	private static PatternGraph getPrepareStateWithPkKpPattern() { // #2.3.8 (all matches; don't repeat) - could also be repeated
		// gtr for adding new calculated labels
		PatternGraph gtr = new PatternGraph("prepare elimination of state (with just pk, kp)");
		PatternNode p = new PatternNode("#{current} == 1 && #{used} != 1").addPatternAttribute(new PatternAttribute().setAction("+").setName("used").setValue(true));
		PatternNode k = new PatternNode("#{eliminate} == 1");
		gtr.addPatternNode(p, k);
		/* CASE 5: there's pp, pk, kk and kp */
		k.addPatternEdge("==", "#{pk}", k);
		k.addPatternEdge("-", "#{kp}", p);
		p.addPatternEdge("+", "'(' + #{pk} + ')(' + #{kp} + ')'", p);
		return gtr;
	}

	private static PatternGraph getUnmarkCurrentPattern() { // #2.4
		PatternGraph gtr = new PatternGraph("remove mark of current working state");
		PatternNode n = new PatternNode("#{current} == 1");
		n.addPatternAttribute(new PatternAttribute().setAction("-").setName("current"));
		n.addPatternAttribute(new PatternAttribute().setAction("+").setName("past").setValue(true));
		gtr.addPatternNode(n);
		return gtr;
	}
	
	private static PatternGraph getRemoveMarksPattern() { // #2.5 (all matches; don't repeat) - could also be repeated
		// gtr for removing marks
		PatternGraph gtr = new PatternGraph("remove mark of used state");
		PatternNode n = new PatternNode("#{used} == 1").addPatternAttribute(new PatternAttribute().setAction("-").setName("used"));
		gtr.addPatternNode(n);
		return gtr;
	}
	
	private static PatternGraph getEliminateMarkedStatePattern() { // #2.6 (single match; don't repeat) - could also be repeated
		// gtr for eliminating the previously marked state
		PatternGraph gtr = new PatternGraph("eliminate state itself");
		PatternNode k = new PatternNode("#{eliminate} == 1").setAction("-");
		gtr.addPatternNode(k);
		return gtr;
	}
	
	private static PatternGraph getUnmarkPastPattern() { // #2.7
		PatternGraph gtr = new PatternGraph("remove mark of past working state");
		PatternNode n = new PatternNode("#{past} == 1");
		n.addPatternAttribute(new PatternAttribute().setAction("-").setName("past"));
		gtr.addPatternNode(n);
		return gtr;
	}
	
}