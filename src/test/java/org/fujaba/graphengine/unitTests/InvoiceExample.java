package org.fujaba.graphengine.unitTests;

import java.util.ArrayList;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.pattern.PatternAttribute;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;
import org.junit.Assert;
import org.junit.Test;

public class InvoiceExample {
	
	private Graph getInvoiceStartGraph() {
		Graph g = new Graph();
		Node product1 = new Node()
				.setAttribute("type", "Product")
				.setAttribute("name", "P1")
				.setAttribute("netPrice", 10)
				.setAttribute("tax", 0.19)
				.setAttribute("amount", 2)
				.setAttribute("total", 23.8);
		Node product2 = new Node()
				.setAttribute("type", "Product")
				.setAttribute("name", "P2")
				.setAttribute("netPrice", 5)
				.setAttribute("tax", 0.19)
				.setAttribute("amount", 4)
				.setAttribute("total", 23.7);
		Node invoice = new Node()
				.setAttribute("type", "Invoice")
				.addEdge("item", product1, product2);
		g.addNode(invoice, product1, product2);
		return g;
	}
	
	private PatternGraph getProductTotalIsWrongPattern() {
		PatternGraph p = new PatternGraph("w");
		p.addPatternNode(new PatternNode("#{type} == 'Product' && round(100 * (#{netPrice} * (1 + #{tax}) * #{amount})) / 100 != #{total}")
				.addPatternAttribute(new PatternAttribute().setAction("+").setName("status").setValue("wrong")));
		return p;
	}
	
	private PatternGraph getProductTotalIsRightPattern() {
		PatternGraph p = new PatternGraph("r");
		p.addPatternNode(new PatternNode("#{type} == 'Product' && round(100 * (#{netPrice} * (1 + #{tax}) * #{amount})) / 100 == #{total}")
				.addPatternAttribute(new PatternAttribute().setAction("+").setName("status").setValue("right")));
		return p;
	}
	
	private PatternGraph getInvoiceHasErrorPattern() {
		PatternGraph p = new PatternGraph("s");
		PatternNode n1 = new PatternNode("#{type} == 'Product' && #{status} == 'wrong'");
		PatternNode n2 = new PatternNode("#{type} == 'Invoice'")
				.addPatternAttribute(new PatternAttribute().setAction("+").setName("status").setValue("error"))
				.addPatternEdge("item", n1);
		p.addPatternNode(n1, n2);
		return p;
	}
	
	private PatternGraph getInvoiceHasSuccessPattern() {
		PatternGraph p = new PatternGraph("s");
		PatternNode n1 = new PatternNode("#{type} == 'Product' && #{status} == 'wrong'").setAction("!=");
		PatternNode n2 = new PatternNode("#{type} == 'Invoice'")
				.addPatternAttribute(new PatternAttribute().setAction("+").setName("status").setValue("success"))
				.addPatternEdge("item", n1);
		p.addPatternNode(n1, n2);
		return p;
	}
	
	@Test
	public void testInvoiceExample() {
		
		ArrayList<ArrayList<PatternGraph>> patterns = new ArrayList<>();
	    patterns.add(new ArrayList<>());

	    patterns.get(0).add(getProductTotalIsWrongPattern());
	    patterns.get(0).add(getProductTotalIsRightPattern());
	    patterns.get(0).add(getInvoiceHasErrorPattern());
	    patterns.get(0).add(getInvoiceHasSuccessPattern());
	    
	    Graph reachabilityGraph = PatternEngine.calculateReachabilityGraph(getInvoiceStartGraph(), patterns);
	    Assert.assertEquals(10, reachabilityGraph.getNodes().size());
	    GraphEngine.prepareGraphAsJsonFileForSigmaJs(reachabilityGraph, "data.json");
	    
	}
	

}
