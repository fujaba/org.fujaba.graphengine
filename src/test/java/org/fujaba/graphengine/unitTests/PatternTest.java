package org.fujaba.graphengine.unitTests;

import org.junit.Test;

import java.util.ArrayList;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.Match;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.pattern.PatternAttribute;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;
import org.junit.Assert;

/**
 * This class is for testing Patterns.
 * 
 * @author Philipp Kolodziej
 */
public class PatternTest {
	
	@Test
	public void testPatternSerialization() {
		PatternGraph patternGraph = getTranportRule();
		String toJson = GraphEngine.getGson().toJson(patternGraph); // hand-made graph to json
		
		PatternGraph fromJson = GraphEngine.getGson().fromJson(toJson, PatternGraph.class); // json from hand-made to object
		String backToJson = GraphEngine.getGson().toJson(fromJson); // automaticly built object to json
		
		PatternGraph fromJson2 = GraphEngine.getGson().fromJson(backToJson, PatternGraph.class); // json from automaticly built object to object
		String backToJson2 = GraphEngine.getGson().toJson(fromJson2); // automaticly built object to json
		
		Assert.assertEquals(backToJson, backToJson2);
		
		// TODO: implement better test for PatternGraph comparison
//		System.out.println(patternGraph);
	}
	
	@Test
	public void testPatternMatching() {
		PatternGraph transportRule = getTranportRule();
		Graph ferrymansGraph = getFerrymansGraph();
		
		ArrayList<Match> matches = PatternEngine.matchPattern(ferrymansGraph, transportRule, true);
		Assert.assertEquals(1, matches.size()); // finds a match (at all) for the transport rule
		
		matches = PatternEngine.matchPattern(ferrymansGraph, transportRule, false);
		Assert.assertEquals(3, matches.size()); // finds all 3 matches for the transport rule
	}
	
	@Test
	public void testApplyingMatches() {
		PatternGraph transportRule = getTranportRule();
		Graph ferrymansGraph = getFerrymansGraph();
		
		ArrayList<Match> matches = PatternEngine.matchPattern(ferrymansGraph, transportRule, false);
		
		Assert.assertEquals(3, matches.size());

		Graph wolfTransported = PatternEngine.applyMatch(matches.get(0));
//		System.out.println(wolfTransported);
		Graph goatTransported = PatternEngine.applyMatch(matches.get(1));
//		System.out.println(goatTransported);
		Graph cabbageTransported = PatternEngine.applyMatch(matches.get(2));
//		System.out.println(cabbageTransported);

		Assert.assertTrue(!GraphEngine.isIsomorphTo(ferrymansGraph, wolfTransported));
		Assert.assertTrue(!GraphEngine.isIsomorphTo(ferrymansGraph, goatTransported));
		Assert.assertTrue(!GraphEngine.isIsomorphTo(ferrymansGraph, cabbageTransported));
	}
	
	@Test
	public void testCycleFreeRepetitivePatternApplication() {
		PatternGraph eatingRule = getEatingRule();
		PatternGraph transportRule = getTranportRule();
		PatternGraph emptyTransportRule = getEmptyTranportRule();
		Graph ferrymansGraph = getFerrymansGraph();
		ArrayList<PatternGraph> patterns = new ArrayList<PatternGraph>();
		patterns.add(eatingRule);
		patterns.add(transportRule);
		patterns.add(emptyTransportRule);

		Graph result = PatternEngine.applyPatterns(ferrymansGraph, patterns, false);
//		System.out.println(result);
		Assert.assertTrue(!GraphEngine.isIsomorphTo(ferrymansGraph, result));
	}
	
	@Test
	public void testSolvingFerrymansProblem() {
		PatternGraph eatingRule = getEatingRule();
		PatternGraph transportRule = getTranportRule();
		PatternGraph emptyTransportRule = getEmptyTranportRule();
		Graph ferrymansGraph = getFerrymansGraph();
		Graph ferrymansSolutionGraph = getFerrymansSolutionGraph();
		ArrayList<Match> matches;
		
		Graph current = ferrymansGraph;
		
		matches = PatternEngine.matchPattern(current, eatingRule, false);
		Assert.assertEquals(0, matches.size()); // noone gets eaten
		
		matches = PatternEngine.matchPattern(current, transportRule, false);
		Assert.assertEquals(3, matches.size()); // he could bring each species
		
		current = PatternEngine.applyMatch(matches.get(1)); // 1st turn: we know he has to bring the goat first

		matches = PatternEngine.matchPattern(current, eatingRule, false);
		Assert.assertEquals(0, matches.size()); // noone gets eaten

		matches = PatternEngine.matchPattern(current, emptyTransportRule, false);
		Assert.assertEquals(1, matches.size()); // he could go back alone

		current = PatternEngine.applyMatch(matches.get(0)); // 2nd turn: he has to go back

		matches = PatternEngine.matchPattern(current, eatingRule, false);
		Assert.assertEquals(0, matches.size()); // noone gets eaten
		
		matches = PatternEngine.matchPattern(current, transportRule, false);
		Assert.assertEquals(2, matches.size()); // he could now bring wolf or cabbage

		current = PatternEngine.applyMatch(matches.get(0)); // 3rd turn: he brings the wolf

		matches = PatternEngine.matchPattern(current, eatingRule, false);
		Assert.assertEquals(0, matches.size()); // noone gets eaten
		
		matches = PatternEngine.matchPattern(current, transportRule, false);
		Assert.assertEquals(2, matches.size()); // he could now bring back wolf or goat

		current = PatternEngine.applyMatch(matches.get(1)); // 4th turn: he brings back the goat

		matches = PatternEngine.matchPattern(current, eatingRule, false);
		Assert.assertEquals(0, matches.size()); // noone gets eaten
		
		matches = PatternEngine.matchPattern(current, transportRule, false);
		Assert.assertEquals(2, matches.size()); // he could now bring goat or cabbage

		current = PatternEngine.applyMatch(matches.get(1)); // 5th turn: he brings the cabbage

		matches = PatternEngine.matchPattern(current, eatingRule, false);
		Assert.assertEquals(0, matches.size()); // noone gets eaten

		matches = PatternEngine.matchPattern(current, emptyTransportRule, false);
		Assert.assertEquals(1, matches.size()); // he could go back alone

		current = PatternEngine.applyMatch(matches.get(0)); // 6th turn: he has to go back

		matches = PatternEngine.matchPattern(current, eatingRule, false);
		Assert.assertEquals(0, matches.size()); // noone gets eaten
		
		matches = PatternEngine.matchPattern(current, transportRule, false);
		Assert.assertEquals(1, matches.size()); // he could now finally bring the goat and he's done

		current = PatternEngine.applyMatch(matches.get(0)); // 7th turn: he brings the goat and completes the challenge

		matches = PatternEngine.matchPattern(current, eatingRule, false);
		Assert.assertEquals(0, matches.size()); // noone gets eaten
		
		matches = PatternEngine.matchPattern(current, transportRule, false);
		Assert.assertEquals(3, matches.size()); // he could now start over and bring each species
		
		Assert.assertTrue(GraphEngine.isIsomorphTo(ferrymansSolutionGraph, current)); // the solution is as expected
	}
	
	private PatternGraph getEatingRule() {
		PatternNode cargoEats = new PatternNode(), cargoGetsEaten = new PatternNode(), ferry = new PatternNode(), bank = new PatternNode();
		return new PatternGraph()
		.addPatternNode(cargoEats.setAction("==").addPatternAttribute(new PatternAttribute()
				.setAction("==")
				.setName("type")
				.setValue("Cargo")
		).addPatternEdge(new PatternEdge()
				.setAction("==")
				.setSource(cargoEats)
				.setName("at")
				.setTarget(bank)
		).addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(cargoEats)
				.setName("eats")
				.setTarget(cargoGetsEaten)
		)).addPatternNode(cargoGetsEaten.setAction("-").addPatternAttribute(new PatternAttribute()
				.setAction("-")
				.setName("type")
				.setValue("Cargo")
		).addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(cargoGetsEaten)
				.setName("at")
				.setTarget(bank)
		)).addPatternNode(ferry.setAction("==").addPatternAttribute(new PatternAttribute()
				.setAction("==")
				.setName("type")
				.setValue("Ferry")
		).addPatternEdge(new PatternEdge()
				.setAction("!=")
				.setSource(ferry)
				.setName("at")
				.setTarget(bank)
		)).addPatternNode(bank.setAction("==").addPatternAttribute(new PatternAttribute()
				.setAction("==")
				.setName("type")
				.setValue("Bank")
		));
	}
	
	private PatternGraph getTranportRule() {
		PatternNode cargo = new PatternNode(), ferry = new PatternNode(), bankHere = new PatternNode(), bankThere = new PatternNode();
		return new PatternGraph()
		.addPatternNode(cargo.setAction("==").addPatternAttribute(new PatternAttribute()
				.setAction("==")
				.setName("type")
				.setValue("Cargo")
		).addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(cargo)
				.setName("at")
				.setTarget(bankHere)
		).addPatternEdge(new PatternEdge()
				.setAction("+")
				.setSource(cargo)
				.setName("at")
				.setTarget(bankThere)
		)).addPatternNode(ferry.setAction("==").addPatternAttribute(new PatternAttribute()
				.setAction("==")
				.setName("type")
				.setValue("Ferry")
		).addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankHere)
		).addPatternEdge(new PatternEdge()
				.setAction("+")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankThere)
		)).addPatternNode(bankHere.setAction("==").addPatternAttribute(new PatternAttribute()
				.setAction("==")
				.setName("type")
				.setValue("Bank")
		).addPatternEdge(new PatternEdge()
				.setAction("==")
				.setSource(bankHere)
				.setName("opposite")
				.setTarget(bankThere)
		)).addPatternNode(bankThere.setAction("==").addPatternAttribute(new PatternAttribute()
				.setAction("==")
				.setName("type")
				.setValue("Bank")
		));
	}
	
	private PatternGraph getEmptyTranportRule() {
		PatternNode ferry = new PatternNode(), bankHere = new PatternNode(), bankThere = new PatternNode();
		return new PatternGraph()
		.addPatternNode(ferry.setAction("==").addPatternAttribute(new PatternAttribute()
				.setAction("==")
				.setName("type")
				.setValue("Ferry")
		).addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankHere)
		).addPatternEdge(new PatternEdge()
				.setAction("+")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankThere)
		)).addPatternNode(bankHere.setAction("==").addPatternAttribute(new PatternAttribute()
				.setAction("==")
				.setName("type")
				.setValue("Bank")
		).addPatternEdge(new PatternEdge()
				.setAction("==")
				.setSource(bankHere)
				.setName("opposite")
				.setTarget(bankThere)
		)).addPatternNode(bankThere.setAction("==").addPatternAttribute(new PatternAttribute()
				.setAction("==")
				.setName("type")
				.setValue("Bank")
		));
	}
	
	private Graph getFerrymansGraph() {
		Graph ferrymansGraph = new Graph();
		Node wolf = new Node(), goat = new Node(), cabbage = new Node(), ferry = new Node(), north = new Node(), south = new Node();
		ferrymansGraph.addNode(wolf).addNode(goat).addNode(cabbage).addNode(ferry).addNode(north).addNode(south);
		wolf.setAttribute("type", "Cargo").setAttribute("species", "Wolf").addEdge("eats", goat).addEdge("at", north);
		goat.setAttribute("type", "Cargo").setAttribute("species", "Goat").addEdge("eats", cabbage).addEdge("at", north);
		cabbage.setAttribute("type", "Cargo").setAttribute("species", "Cabbage").addEdge("at", north);
		ferry.setAttribute("type", "Ferry").addEdge("at", north);
		north.setAttribute("type", "Bank").setAttribute("side", "north").addEdge("opposite", south);
		south.setAttribute("type", "Bank").setAttribute("side", "south").addEdge("opposite", north);
		return ferrymansGraph;
	}
	
	private Graph getFerrymansSolutionGraph() {
		Graph ferrymansGraph = new Graph();
		Node wolf = new Node(), goat = new Node(), cabbage = new Node(), ferry = new Node(), north = new Node(), south = new Node();
		ferrymansGraph.addNode(wolf).addNode(goat).addNode(cabbage).addNode(ferry).addNode(north).addNode(south);
		wolf.setAttribute("type", "Cargo").setAttribute("species", "Wolf").addEdge("eats", goat).addEdge("at", south);
		goat.setAttribute("type", "Cargo").setAttribute("species", "Goat").addEdge("eats", cabbage).addEdge("at", south);
		cabbage.setAttribute("type", "Cargo").setAttribute("species", "Cabbage").addEdge("at", south);
		ferry.setAttribute("type", "Ferry").addEdge("at", south);
		north.setAttribute("type", "Bank").setAttribute("side", "north").addEdge("opposite", south);
		south.setAttribute("type", "Bank").setAttribute("side", "south").addEdge("opposite", north);
		return ferrymansGraph;
	}
	
}