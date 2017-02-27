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

/**
 * Template for a Live Demo.
 * 
 * 
 * Intended for programmatically creating
 * - a start graph
 * - several GTR
 * - a RG using the start graph and the GTR
 * - a test, verifying the RG
 * 
 * 
 * 
 * The intended example features a start graph as follows:
 * - one node working as an invoice object (with attribute: "type")
 * - two nodes representing a product (with attributes: "type", "name", "netPrice", "tax", "amount", "total")
 * - edges with name "item" from the invoice to its products
 * 
 * 
 * 
 * Also there are a few GTR:
 * 
 * - falsifying a product's total price (total != Math.round(netPrice * (1 + tax) * amount * 100) / 100)
 *   => setting its "status" attribute to "wrong"
 *   
 * - verifying a product's total price (total == Math.round(netPrice * (1 + tax) * amount * 100) / 100)
 *   => setting its "status" attribute to "right"
 *   
 * - falsifying an invoice's status (any of its products has status "wrong")
 *   => setting its "status" attribute to "error"
 *   
 * - verifying an invoice's status (none of its products has status "wrong")
 *   => setting its "status" attribute to "success"
 *   
 *   
 *   
 * The resulting reachability graph should have 10 states, one of which is 'final' with the only outgoing edge pointing to itself.
 * 
 * @author Philipp Kolodziej
 */
public class LiveInvoiceExample {
	
	
	
	private Graph getInvoiceStartGraph() {
		Graph g = new Graph();
		Node n = new Node();
		g.addNode(n);
		return g;
	}
	
	private PatternGraph getProductTotalIsWrongPattern() {
		PatternGraph p = new PatternGraph("wrong");
		PatternNode pn = new PatternNode();
		p.addPatternNode(pn);
		return p;
	}
	
	private PatternGraph getProductTotalIsRightPattern() {
		PatternGraph p = new PatternGraph("right");
		PatternNode pn = new PatternNode();
		p.addPatternNode(pn);
		return p;
	}
	
	private PatternGraph getInvoiceHasErrorPattern() {
		PatternGraph p = new PatternGraph("error");
		PatternNode pn = new PatternNode();
		p.addPatternNode(pn);
		return p;
	}
	
	private PatternGraph getInvoiceHasSuccessPattern() {
		PatternGraph p = new PatternGraph("success");
		PatternNode pn = new PatternNode();
		p.addPatternNode(pn);
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
