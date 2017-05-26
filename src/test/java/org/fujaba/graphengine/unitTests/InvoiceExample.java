package org.fujaba.graphengine.unitTests;

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
 * Finished example for a Live Demo.
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
		PatternGraph p = new PatternGraph("wrong");
		p.addPatternNode(new PatternNode("#{type} == 'Product' && round(100 * (#{netPrice} * (1 + #{tax}) * #{amount})) / 100 != #{total}")
				.addPatternAttribute(new PatternAttribute().setAction("+").setName("status").setValue("wrong")));
		return p;
	}
	
	private PatternGraph getProductTotalIsRightPattern() {
		PatternGraph p = new PatternGraph("right");
		p.addPatternNode(new PatternNode("#{type} == 'Product' && round(100 * (#{netPrice} * (1 + #{tax}) * #{amount})) / 100 == #{total}")
				.addPatternAttribute(new PatternAttribute().setAction("+").setName("status").setValue("right")));
		return p;
	}
	
	private PatternGraph getInvoiceHasErrorPattern() {
		PatternGraph p = new PatternGraph("error");
		PatternNode n1 = new PatternNode("#{type} == 'Product' && #{status} == 'wrong'");
		PatternNode n2 = new PatternNode("#{type} == 'Invoice'")
				.addPatternAttribute(new PatternAttribute().setAction("+").setName("status").setValue("error"))
				.addPatternEdge("item", n1);
		p.addPatternNode(n1, n2);
		return p;
	}
	
	private PatternGraph getInvoiceHasSuccessPattern() {
		PatternGraph p = new PatternGraph("success");
		PatternNode n1 = new PatternNode("#{type} == 'Product' && #{status} == 'wrong'").setAction("!=");
		PatternNode n2 = new PatternNode("#{type} == 'Invoice'")
				.addPatternAttribute(new PatternAttribute().setAction("+").setName("status").setValue("success"))
				.addPatternEdge("item", n1);
		p.addPatternNode(n1, n2);
		return p;
	}
	
	@Test
	public void testInvoiceExample() {
	    
	    Graph reachabilityGraph = PatternEngine.calculateReachabilityGraph(
	    		getInvoiceStartGraph(),
	    		getProductTotalIsWrongPattern(),
	    		getProductTotalIsRightPattern(),
	    		getInvoiceHasErrorPattern(),
	    		getInvoiceHasSuccessPattern()
	    );
	    
	    Assert.assertEquals(10, reachabilityGraph.getNodes().size());
	    
	    GraphEngine.prepareGraphAsJsonFileForSigmaJs(reachabilityGraph, "data.json");
	    
	}
	
	

}
