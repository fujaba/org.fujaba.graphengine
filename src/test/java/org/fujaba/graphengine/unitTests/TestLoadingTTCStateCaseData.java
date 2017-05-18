package org.fujaba.graphengine.unitTests;

import java.util.ArrayList;

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

public class TestLoadingTTCStateCaseData {
	
	@Test
	public void testLoadingTTCStateCaseData() {
		String taskMainPath = "src/main/resources/ExperimentalData/testdata/emf/task-main/";
		Graph g;
		
		/*
		 * just loading models and count nodes:
		 */
		
		g = TTCStateCaseGraphLoader.load(taskMainPath + "leader3_2.xmi");
		Assert.assertEquals(26, g.getNodes().size());
		
		System.out.println(g);
		
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
	public void testTransformingTTCStateCaseData() {

		// get data:
		String taskMainPath = "src/main/resources/ExperimentalData/testdata/emf/task-main/";
		Graph g = TTCStateCaseGraphLoader.load(taskMainPath + "leader3_2.xmi");
		System.out.println("loaded:\n" + g + "\n");
		
		// gtr for new initial state:
		PatternGraph gtrInitial = new PatternGraph("new initial state");
		PatternNode initialNode = new PatternNode("#{initial} == 1").addPatternAttribute(new PatternAttribute().setAction("-").setName("initial"));
		PatternNode newInitialNode = new PatternNode().setAction("+").addPatternAttribute(new PatternAttribute().setAction("+").setName("newInitial").setValue(true));
		PatternNode noExistingNewInitialNode = new PatternNode("#{newInitial} == 1").setAction("!=");
		gtrInitial.addPatternNode(initialNode, newInitialNode, noExistingNewInitialNode);
		newInitialNode.addPatternEdge("+", "", initialNode);
		ArrayList<Match> matches = PatternEngine.matchPattern(g, gtrInitial, true); // just a single match
		Assert.assertEquals(1, matches.size());
		g = PatternEngine.applyMatch(matches.get(0));
		System.out.println("added new initial state (and removed 'initial' flag for the old one):\n" + g + "\n");
		
		// gtr for new final state:
		PatternGraph gtrFinal = new PatternGraph("new final state");
		PatternNode finalNode = new PatternNode("#{final} == 1").addPatternAttribute(new PatternAttribute().setAction("-").setName("final"));
		PatternNode newFinalNode = new PatternNode().setAction("+").addPatternAttribute(new PatternAttribute().setAction("+").setName("newFinal").setValue(true));
		PatternNode noExistingNewFinalNode = new PatternNode("#{newFinal} == 1").setAction("!=");
		gtrFinal.addPatternNode(finalNode, newFinalNode, noExistingNewFinalNode);
		finalNode.addPatternEdge("+", "", newFinalNode);
		matches = PatternEngine.matchPattern(g, gtrFinal, true); // just a single match
		Assert.assertEquals(1, matches.size());
		g = PatternEngine.applyMatch(matches.get(0));
		System.out.println("added new final state (and removed 'final' flag for the old one):\n" + g + "\n");
		
		// gtr for adding to new final state:
		PatternGraph gtrOtherFinal = new PatternGraph("adding to the existing new final state");
		PatternNode otherFinalNode = new PatternNode("#{final == 1}").addPatternAttribute(new PatternAttribute().setAction("-").setName("final"));
		PatternNode existingNewFinalNode = new PatternNode("#{newFinal} == 1");
		gtrOtherFinal.addPatternNode(otherFinalNode, existingNewFinalNode);
		otherFinalNode.addPatternEdge("+", "", existingNewFinalNode);
		do {
			matches = PatternEngine.matchPattern(g, gtrOtherFinal, true); // just a single match (each time)
			if (matches != null && matches.size() > 0) {
				g = PatternEngine.applyMatch(matches.get(0));
				System.out.println("added edge to the new final state (and removed 'final' flag for the other one):\n" + g + "\n");
			} else {
				break;
			}
		} while (true);
		
		// gtr for joining multiple edges between the same two nodes
		/**
		 * this doesn't seem possible right now. you can't have a PatternGraph that says:
		 * 'find two (different) edges with any label between some nodes!'
		 * 
		 * so we unfortunately seem to have to do this by hand:
		 */
		ArrayList<Node> sources = new ArrayList<Node>();
		ArrayList<Node> targets = new ArrayList<Node>();
		ArrayList<String> firstLabels = new ArrayList<String>();
		ArrayList<String> secondLabels = new ArrayList<String>();
		for (Node source: g.getNodes()) {
			for (Node target: g.getNodes()) {
				for (String sourceLabel1: source.getEdges().keySet()) {
					if (source.getEdges(sourceLabel1).contains(target)) {
						for (String sourceLabel2: source.getEdges().keySet()) {
							if (!sourceLabel1.equals(sourceLabel2) && source.getEdges(sourceLabel2).contains(target)) {
								sources.add(source);
								targets.add(target);
								firstLabels.add(sourceLabel1);
								secondLabels.add(sourceLabel2);
							}
						}
					}
				}
			}
		}
		for (int i = 0; i < sources.size(); ++i) {
			sources.get(i).removeEdge(firstLabels.get(i), targets.get(i));
			sources.get(i).removeEdge(secondLabels.get(i), targets.get(i));
			sources.get(i).addEdge("[(" + firstLabels.get(i) + ")(" + secondLabels.get(i) + ")]", targets.get(i));
		}
		System.out.println("after joining multiple labels with the same sources and same targets:\n" + g + "\n");
		
		// gtr for state elimination itself (the q->k->p to q->p case)
		PatternGraph gtrEliminate = new PatternGraph("eliminate state");
		PatternNode p = new PatternNode();
		PatternNode k = new PatternNode().setAction("-");
		PatternNode q = new PatternNode();
		gtrEliminate.addPatternNode(p, k, q);
		p.addPatternEdge("-", (String)null, k);
		k.addPatternEdge("-", (String)null, q);
		p.addPatternEdge("+", "calculated", q);
		
		do {
			matches = PatternEngine.matchPattern(g, gtrEliminate, true); // just a single match (each time)
			if (matches != null && matches.size() > 0) {
				g = PatternEngine.applyMatch(matches.get(0));
				System.out.println("eliminated a state (q->k->p to q->p):\n" + g + "\n");
			} else {
				break;
			}
		} while (true);
		
		// TODO: gtr for state elimination itself (the q->k->q to q->q case)
		
		System.out.println("final graph: " + g);
		
	}
	
	
}