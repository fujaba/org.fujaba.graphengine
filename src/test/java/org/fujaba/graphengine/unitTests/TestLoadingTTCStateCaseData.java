package org.fujaba.graphengine.unitTests;

import java.util.ArrayList;

import org.fujaba.graphengine.Match;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.graph.Graph;
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

		String taskMainPath = "src/main/resources/ExperimentalData/testdata/emf/task-main/";
		Graph g = TTCStateCaseGraphLoader.load(taskMainPath + "leader3_2.xmi");
		
		PatternGraph gtr1 = new PatternGraph("gtr1");
		
		PatternNode p = new PatternNode();
		PatternNode k = new PatternNode();
		PatternNode q = new PatternNode();
		
		gtr1.addPatternNode(p, k, q);
		
		p.addPatternEdge("-", (String)null, k);
		k.addPatternEdge("-", (String)null, q);
		p.addPatternEdge("+", "calculated", q);
		
		ArrayList<Match> matches = PatternEngine.matchPattern(g, gtr1, false);
		
		Assert.assertTrue(matches.size() > 0);
		
		System.out.println("yay, " + matches.size() + " matches!");
		
	}
	
	
}