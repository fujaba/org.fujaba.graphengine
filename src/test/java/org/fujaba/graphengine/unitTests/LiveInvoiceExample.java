package org.fujaba.graphengine.unitTests;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;
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
	
	
	
	@SuppressWarnings("unused")
	private Graph getInvoiceStartGraph() {
		Graph g = new Graph();
		Node n = new Node();
		g.addNode(n);
		return g;
	}

	@SuppressWarnings("unused")
	private PatternGraph getProductTotalIsWrongPattern() {
		PatternGraph p = new PatternGraph("wrong");
		PatternNode pn = new PatternNode();
		p.addPatternNode(pn);
		return p;
	}

	@SuppressWarnings("unused")
	private PatternGraph getProductTotalIsRightPattern() {
		PatternGraph p = new PatternGraph("right");
		PatternNode pn = new PatternNode();
		p.addPatternNode(pn);
		return p;
	}

	@SuppressWarnings("unused")
	private PatternGraph getInvoiceHasErrorPattern() {
		PatternGraph p = new PatternGraph("error");
		PatternNode pn = new PatternNode();
		p.addPatternNode(pn);
		return p;
	}

	@SuppressWarnings("unused")
	private PatternGraph getInvoiceHasSuccessPattern() {
		PatternGraph p = new PatternGraph("success");
		PatternNode pn = new PatternNode();
		p.addPatternNode(pn);
		return p;
	}
	
	@Test
	public void testInvoiceExample() {
		
	    Graph reachabilityGraph = new Graph();
	    
	    GraphEngine.prepareGraphAsJsonFileForSigmaJs(reachabilityGraph, "data.json");
	    
	}
	

	
}
