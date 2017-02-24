package org.fujaba.graphengine.unitTests;

import org.junit.Test;
import org.fujaba.graphengine.PatternEngine;
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
		Graph graph = getAdjustedFerrymansGraph();
		
		String expression = "#{type} == 'Cargo' && #{species} == 'Wolf' && (#{count} < 5 || #{count} > 1000) && #{existant} == 1.0";

		boolean matched = false;
		for (Node node: graph.getNodes()) {
			matched = PatternEngine.evaluate(node, expression);
			if (matched) {
				break;
			}
		}
		Assert.assertTrue(matched);
	}

	@Test
	public void testComplexExpression() {
		Node n = new Node();
		n.setAttribute("type", "Product");
		n.setAttribute("name", "Raspberry Pi 3");
		n.setAttribute("netPrice", 30.25);
		n.setAttribute("tax", 0.19);
		n.setAttribute("amount", 3);
		
		Assert.assertTrue(PatternEngine.evaluate(n, "#{type} == 'Product' && #{netPrice} * (1 + #{tax}) * #{amount} > 100"));
		Assert.assertTrue(PatternEngine.evaluate(n, "#{type} == 'Product' && indexOf(#{name}, 'Raspberry Pi', 0) != -1"));
	}
	
	/**
	 * Method to obtain an adjusted initial situation of the ferryman's problem as a graph.
	 * @return an adjusted initial situation of the ferryman's problem as a graph.
	 */
	private Graph getAdjustedFerrymansGraph() {
		Graph ferrymansGraph = new Graph();
		Node wolf = new Node(), goat = new Node(), cabbage = new Node(), ferry = new Node(), north = new Node(), south = new Node();
		ferrymansGraph.addNode(wolf).addNode(goat).addNode(cabbage).addNode(ferry).addNode(north).addNode(south);
		wolf.setAttribute("type", "Cargo").setAttribute("species", "Wolf").setAttribute("count", 1).setAttribute("existant", true).addEdge("eats", goat).addEdge("at", north);
		goat.setAttribute("type", "Cargo").setAttribute("species", "Goat").setAttribute("count", 1).setAttribute("existant", true).addEdge("eats", cabbage).addEdge("at", north);
		cabbage.setAttribute("type", "Cargo").setAttribute("species", "Cabbage").setAttribute("count", 5).setAttribute("existant", true).addEdge("at", north);
		ferry.setAttribute("type", "Ferry").setAttribute("count", 1).setAttribute("existant", true).addEdge("at", north);
		north.setAttribute("type", "Bank").setAttribute("count", 1).setAttribute("existant", true).setAttribute("side", "north").addEdge("opposite", south);
		south.setAttribute("type", "Bank").setAttribute("count", 1).setAttribute("existant", true).setAttribute("side", "south").addEdge("opposite", north);
		return ferrymansGraph;
	}
	
}
