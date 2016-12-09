package org.fujaba.graphengine.unitTests;

import org.junit.Test;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.junit.Assert;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

public class TestUseExpressionLibrary {

	
	
	@Test
	public void testUseExpressionLibrary() {
		Evaluator evaluator = new Evaluator();
		try {
			/**
			 * Add the variables to our instance of the Evaluator class.
			 */
			evaluator.putVariable("a", "'Hello'");
			evaluator.putVariable("b", "'World'");
			evaluator.putVariable("c", "7");
			evaluator.putVariable("d", "3.5");
			evaluator.putVariable("e", "1");
			evaluator.putVariable("f", "0");
			/**
			 * This sample simply outputs the variables.
			 */
			System.out.println(evaluator.evaluate("#{a}"));
			System.out.println(evaluator.evaluate("#{b}"));
			/**
			 * This sample outputs a preloaded math varibles.
			 */
			System.out.println(evaluator.evaluate("#{PI}"));
			/**
			 * This sample adds the variables together to form a sentence.
			 */
			System.out.println(evaluator.evaluate("#{c} + #{d} == 10.5"));
			System.out.println(evaluator.evaluate("#{c} * #{d} == 24.5"));
			
			System.out.println(evaluator.evaluate("#{e} && #{f}"));
			System.out.println(evaluator.evaluate("#{e} || #{f}"));
		} catch (EvaluationException ee) {
			System.out.println(ee);
		}
		
	}
	
	@Test
	public void testUseExpressionLibraryOnFerrymansGraph() {
		Graph graph = getFerrymansGraph();
		
		String expression = "#{type} == 'Cargo' && #{species} == 'Wolf' && (#{count} < 5 || #{count} > 1000) && #{existant} == 1.0";

		boolean matched = false;
		for (Node node: graph.getNodes()) {
			Evaluator eval = buildNodeEvaluator(node);
			try {
				matched = "1.0".equals(eval.evaluate(expression));
			} catch (Throwable t) {
				matched = false;
			}
			if (matched) {
				break;
			}
		}
		Assert.assertTrue(matched);
	}
	
	private Evaluator buildNodeEvaluator(Node node) {
		Evaluator evaluator = new Evaluator();
		for (String key: node.getAttributes().keySet()) {
			if (node.getAttribute(key) instanceof String) {
				evaluator.putVariable(key, "'" + node.getAttribute(key) + "'");
			} else if (node.getAttribute(key) instanceof Boolean) {
				evaluator.putVariable(key, "" + ((boolean)node.getAttribute(key) ? "1.0" : "0.0"));
			} else {
				evaluator.putVariable(key, "" + node.getAttribute(key));
			}
		}
		return evaluator;
	}
	
	private Graph getFerrymansGraph() {
		Graph ferrymansGraph = new Graph();
		Node wolf = new Node(), goat = new Node(), cabbage = new Node(), ferry = new Node(), north = new Node(), south = new Node();
		ferrymansGraph.addNode(wolf).addNode(goat).addNode(cabbage).addNode(ferry).addNode(north).addNode(south);
		wolf.setAttribute("type", "Cargo").setAttribute("species", "Wolf").setAttribute("count", 1).setAttribute("existant", true).addEdge("eats", goat).addEdge("at", north);
		goat.setAttribute("type", "Cargo").setAttribute("species", "Goat").setAttribute("count", 1).setAttribute("existant", true).addEdge("eats", cabbage).addEdge("at", north);
		cabbage.setAttribute("type", "Cargo").setAttribute("species", "Cabbage").setAttribute("count", 5).setAttribute("existant", true).addEdge("at", north);
		ferry.setAttribute("type", "Ferry").setAttribute("count", 1).setAttribute("existant", true).addEdge("at", north);
		north.setAttribute("type", "Bank").setAttribute("side", "north").setAttribute("count", 1).setAttribute("existant", true).addEdge("opposite", south);
		south.setAttribute("type", "Bank").setAttribute("side", "south").setAttribute("count", 1).setAttribute("existant", true).addEdge("opposite", north);
		return ferrymansGraph;
	}
	
}
