package org.fujaba.graphengine.unitTests;

import java.io.File;

import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.stateelimination.TTCStateCaseGraphLoader;
import org.junit.Assert;
import org.junit.Test;

public class TestLoadingTTCStateCaseData {
	
	@Test
	public void testLoadingTTCStateCaseData() {
		String taskMainPath = "src/main/resources/ExperimentalData/testdata/emf/task-main/";
		Graph g;
		
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
}