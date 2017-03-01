package org.fujaba.graphengine.unitTests;

import org.junit.Test;

import java.util.ArrayList;

import org.fujaba.graphengine.GraphDumper;
import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.Match;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandlerCSPLowHeuristics;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandlerCombinatorial;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandlerSorting;
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
		
		// since there's no check for isomorphism between two PatternGraphs, this only works if the serialized String is the same...
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
	
	@Test
	public void testSolvingFerrymansProblemWithReachabilityGraph() {
		PatternGraph transportRule = getCorrectTranportRule();
		PatternGraph emptyTransportRule = getCorrectEmptyTranportRule();
		Graph ferrymansGraph = getFerrymansGraph();
		ArrayList<ArrayList<PatternGraph>> patterns = new ArrayList<ArrayList<PatternGraph>>();
		ArrayList<PatternGraph> priorityLevel = new ArrayList<PatternGraph>();
		priorityLevel.add(transportRule);
		priorityLevel.add(emptyTransportRule);
		patterns.add(priorityLevel);

		// calculate reachability graph with corrected rules:
		Graph rg = PatternEngine.calculateReachabilityGraph(ferrymansGraph, patterns);
		// check if the solution is contained in the reachability graph:
		Assert.assertNotNull(PatternEngine.findGraphInReachabilityGraph(rg, getFerrymansSolutionGraph()));
		

//	    new GraphDumper(rg).dumpGraph("test.html");
	}
	
	@Test
	public void testNegativePatternVariantsWithDifferentIsomorphismCheckApproaches() {

		// example: find all cars, that have no blue wheel
		
		// build a test graph with 6 cars - with two of them having any blue wheels
		Graph carGraph = new Graph();
		
		// build car 'A' with 4 black wheels:
		Node carA = new Node().setAttribute("type", "Car");
		Node wheelA1 = new Node().setAttribute("type", "Wheel").setAttribute("color", "black");
		Node wheelA2 = new Node().setAttribute("type", "Wheel").setAttribute("color", "black");
		Node wheelA3 = new Node().setAttribute("type", "Wheel").setAttribute("color", "black");
		Node wheelA4 = new Node().setAttribute("type", "Wheel").setAttribute("color", "black");
		carGraph.addNode(carA).addNode(wheelA1).addNode(wheelA2).addNode(wheelA3).addNode(wheelA4);
		carA.addEdge("has", wheelA1).addEdge("has", wheelA2).addEdge("has", wheelA3).addEdge("has", wheelA4);

		// build a car 'B' with 3 black wheels and 1 blue wheel:
		Node carB = new Node().setAttribute("type", "Car");
		Node wheelB1 = new Node().setAttribute("type", "Wheel").setAttribute("color", "blue");
		Node wheelB2 = new Node().setAttribute("type", "Wheel").setAttribute("color", "black");
		Node wheelB3 = new Node().setAttribute("type", "Wheel").setAttribute("color", "black");
		Node wheelB4 = new Node().setAttribute("type", "Wheel").setAttribute("color", "black");
		carGraph.addNode(carB).addNode(wheelB1).addNode(wheelB2).addNode(wheelB3).addNode(wheelB4);
		carB.addEdge("has", wheelB1).addEdge("has", wheelB2).addEdge("has", wheelB3).addEdge("has", wheelB4);
		carA.addEdge("next", carB);

		// build a car 'C' with 4 blue wheels:
		Node carC = new Node().setAttribute("type", "Car");
		Node wheelC1 = new Node().setAttribute("type", "Wheel").setAttribute("color", "blue");
		Node wheelC2 = new Node().setAttribute("type", "Wheel").setAttribute("color", "blue");
		Node wheelC3 = new Node().setAttribute("type", "Wheel").setAttribute("color", "blue");
		Node wheelC4 = new Node().setAttribute("type", "Wheel").setAttribute("color", "blue");
		carGraph.addNode(carC).addNode(wheelC1).addNode(wheelC2).addNode(wheelC3).addNode(wheelC4);
		carC.addEdge("has", wheelC1).addEdge("has", wheelC2).addEdge("has", wheelC3).addEdge("has", wheelC4);
		carB.addEdge("next", carC);

		// build a car 'D' with 4 orange wheels
		Node carD = new Node().setAttribute("type", "Car");
		Node wheelD1 = new Node().setAttribute("type", "Wheel").setAttribute("color", "orange");
		Node wheelD2 = new Node().setAttribute("type", "Wheel").setAttribute("color", "orange");
		Node wheelD3 = new Node().setAttribute("type", "Wheel").setAttribute("color", "orange");
		Node wheelD4 = new Node().setAttribute("type", "Wheel").setAttribute("color", "orange");
		carGraph.addNode(carD).addNode(wheelD1).addNode(wheelD2).addNode(wheelD3).addNode(wheelD4);
		carD.addEdge("has", wheelD1).addEdge("has", wheelD2).addEdge("has", wheelD3).addEdge("has", wheelD4);
		carC.addEdge("next", carD);

		// build a car 'E' with 4 red wheels
		Node carE = new Node().setAttribute("type", "Car");
		Node wheelE1 = new Node().setAttribute("type", "Wheel").setAttribute("color", "red");
		Node wheelE2 = new Node().setAttribute("type", "Wheel").setAttribute("color", "red");
		Node wheelE3 = new Node().setAttribute("type", "Wheel").setAttribute("color", "red");
		Node wheelE4 = new Node().setAttribute("type", "Wheel").setAttribute("color", "red");
		carGraph.addNode(carE).addNode(wheelE1).addNode(wheelE2).addNode(wheelE3).addNode(wheelE4);
		carE.addEdge("has", wheelE1).addEdge("has", wheelE2).addEdge("has", wheelE3).addEdge("has", wheelE4);
		carD.addEdge("next", carE);

		// build a car 'F' with 4 green wheels
		Node carF = new Node().setAttribute("type", "Car");
		Node wheelF1 = new Node().setAttribute("type", "Wheel").setAttribute("color", "green");
		Node wheelF2 = new Node().setAttribute("type", "Wheel").setAttribute("color", "green");
		Node wheelF3 = new Node().setAttribute("type", "Wheel").setAttribute("color", "green");
		Node wheelF4 = new Node().setAttribute("type", "Wheel").setAttribute("color", "green");
		carGraph.addNode(carF).addNode(wheelF1).addNode(wheelF2).addNode(wheelF3).addNode(wheelF4);
		carF.addEdge("has", wheelF1).addEdge("has", wheelF2).addEdge("has", wheelF3).addEdge("has", wheelF4);
		carE.addEdge("next", carF);
		
		// build a pattern that says 'car without a blue wheel':
		PatternGraph carWithoutBlueWheel = new PatternGraph("carWithoutBlueWheel");
		PatternNode car = new PatternNode()
				.setAttributeMatchExpression("#{type} == 'Car'");
		PatternNode wheel = new PatternNode()
				.setAction("!=").setAttributeMatchExpression("#{type} == 'Wheel' && #{color} == 'blue'");
		carWithoutBlueWheel.addPatternNode(car).addPatternNode(wheel);
		car.addPatternEdge(new PatternEdge().setSource(car).setName("has").setTarget(wheel));
		
		long begin;
		double duration;
		// test if only the 4 cars without blue wheels are found:
		begin = System.nanoTime();
		ArrayList<Match> matches = PatternEngine.matchPattern(carGraph, carWithoutBlueWheel, false);
		matches = PatternEngine.matchPattern(carGraph, carWithoutBlueWheel, false);
		duration = (System.nanoTime() - begin) / 1e6;
		System.out.println("PatternEngine.matchPattern(): " + duration + "ms");
		Assert.assertEquals(4, matches.size());

		GraphEngine.prepareGraphAsJsonFileForSigmaJs(carGraph);

		Graph carGraphChangedNodeOrder = carGraph.clone();
		carGraphChangedNodeOrder.getNodes().add(carGraphChangedNodeOrder.getNodes().remove(0));

		begin = System.nanoTime();
		Assert.assertTrue(GraphEngine.isIsomorphTo(carGraph, carGraphChangedNodeOrder));
		duration = (System.nanoTime() - begin) / 1e6;
		System.out.println("GraphEngine: " + duration + "ms");

		begin = System.nanoTime();
		Assert.assertTrue(new IsomorphismHandlerCSPLowHeuristics().isIsomorphTo(carGraph, carGraphChangedNodeOrder));
		duration = (System.nanoTime() - begin) / 1e6;
		System.out.println("IsomorphismHandlerCSPLowHeuristics: " + duration + "ms");
		
		begin = System.nanoTime();
		Assert.assertTrue(new IsomorphismHandlerSorting().isIsomorphTo(carGraph, carGraphChangedNodeOrder));
		duration = (System.nanoTime() - begin) / 1e6;
		System.out.println("IsomorphismHandlerSorting: " + duration + "ms");

//		begin = System.nanoTime();
//		Assert.assertTrue(new IsomorphismHandlerCombinatorial().isIsomorphTo(carGraph, carGraphChangedNodeOrder));
//		duration = (System.nanoTime() - begin) / 1e6;
//		System.out.println("IsomorphismHandlerCombinatorial: " + duration + "ms");

		// test if the reachability graph doesn't go rogue and somehow finds multiple graphs:
		ArrayList<ArrayList<PatternGraph>> patterns = new ArrayList<ArrayList<PatternGraph>>();
		ArrayList<PatternGraph> priorityLevel = new ArrayList<PatternGraph>();
		priorityLevel.add(carWithoutBlueWheel);
		patterns.add(priorityLevel);
		begin = System.nanoTime();
		Graph rg = PatternEngine.calculateReachabilityGraph(carGraph, patterns);
		duration = (System.nanoTime() - begin) / 1e6;
		System.out.println("PatternEngine.calculateReachabilityGraph(): " + duration + "ms");
		Assert.assertEquals(1, rg.getNodes().size());
	}
	
	/**
	 * Method to obtain the 'eating rule' of the ferryman's problem graph.
	 * @return the 'eating rule' of the ferryman's problem graph
	 */
	private PatternGraph getEatingRule() {
		PatternNode cargoEats = new PatternNode(), cargoGetsEaten = new PatternNode(), ferry = new PatternNode(), bank = new PatternNode();
		return new PatternGraph("eatingRule")
		.addPatternNode(cargoEats
				.setAttributeMatchExpression("#{type} == 'Cargo'")
		.addPatternEdge(new PatternEdge()
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
				.setValue("#{type} == 'Cargo'")
		).addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(cargoGetsEaten)
				.setName("at")
				.setTarget(bank)
		)).addPatternNode(ferry
				.setAttributeMatchExpression("#{type} == 'Ferry'")
		.addPatternEdge(new PatternEdge()
				.setAction("!=")
				.setSource(ferry)
				.setName("at")
				.setTarget(bank)
		)).addPatternNode(bank
				.setAttributeMatchExpression("#{type} == 'Bank'")
		);
	}
	
	/**
	 * Method to obtain the (uncorrected) 'transport rule' of the ferryman's problem graph.
	 * Note: This rule allows for the ferryman to leave two species alone, that eat each other!
	 * @return the (uncorrected) 'transport rule' of the ferryman's problem graph
	 */
	private PatternGraph getTranportRule() {
		PatternNode cargo = new PatternNode(), ferry = new PatternNode(), bankHere = new PatternNode(), bankThere = new PatternNode();
		return new PatternGraph("tranportRule")
		.addPatternNode(cargo
				.setAttributeMatchExpression("#{type} == 'Cargo'")
		.addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(cargo)
				.setName("at")
				.setTarget(bankHere)
		).addPatternEdge(new PatternEdge()
				.setAction("+")
				.setSource(cargo)
				.setName("at")
				.setTarget(bankThere)
		)).addPatternNode(ferry
				.setAttributeMatchExpression("#{type} == 'Ferry'")
		.addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankHere)
		).addPatternEdge(new PatternEdge()
				.setAction("+")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankThere)
		)).addPatternNode(bankHere
				.setAttributeMatchExpression("#{type} == 'Bank'")
		.addPatternEdge(new PatternEdge()
				.setSource(bankHere)
				.setName("opposite")
				.setTarget(bankThere)
		)).addPatternNode(bankThere
				.setAttributeMatchExpression("#{type} == 'Bank'")
		);
	}

	/**
	 * Method to obtain the (uncorrected) 'empty transport rule' of the ferryman's problem graph.
	 * Note: This rule allows for the ferryman to leave two species alone, that eat each other!
	 * @return the (uncorrected) 'empty transport rule' of the ferryman's problem graph
	 */
	private PatternGraph getEmptyTranportRule() {
		PatternNode ferry = new PatternNode(), bankHere = new PatternNode(), bankThere = new PatternNode();
		return new PatternGraph("emptyTranportRule")
		.addPatternNode(ferry
				.setAttributeMatchExpression("#{type} == 'Ferry'")
		.addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankHere)
		).addPatternEdge(new PatternEdge()
				.setAction("+")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankThere)
		)).addPatternNode(bankHere
				.setAttributeMatchExpression("#{type} == 'Bank'")
		.addPatternEdge(new PatternEdge()
				.setSource(bankHere)
				.setName("opposite")
				.setTarget(bankThere)
		)).addPatternNode(bankThere
				.setAttributeMatchExpression("#{type} == 'Bank'")
		);
	}

	/**
	 * Method to obtain the (corrected) 'transport rule' of the ferryman's problem graph.
	 * Note: This rule doesn't allow the ferryman to leave two species alone, that eat each other!
	 * @return the (corrected) 'transport rule' of the ferryman's problem graph
	 */
	private PatternGraph getCorrectTranportRule() {
		PatternGraph pattern = new PatternGraph("correctTranportRule");
		PatternNode cargo = new PatternNode(), ferry = new PatternNode(), bankHere = new PatternNode(), bankThere = new PatternNode(), eater = new PatternNode(), getsEaten = new PatternNode();
		pattern.addPatternNode(cargo).addPatternNode(ferry).addPatternNode(bankHere).addPatternNode(bankThere).addPatternNode(eater).addPatternNode(getsEaten);
		cargo.setAttributeMatchExpression("#{type} == 'Cargo'");
		cargo.addPatternEdge(new PatternEdge()
			.setAction("-")
			.setSource(cargo)
			.setName("at")
			.setTarget(bankHere)
		);
		cargo.addPatternEdge(new PatternEdge()
			.setAction("+")
			.setSource(cargo)
			.setName("at")
			.setTarget(bankThere)
		);
		ferry.setAttributeMatchExpression("#{type} == 'Ferry'");
		ferry.addPatternEdge(new PatternEdge()
			.setAction("-")
			.setSource(ferry)
			.setName("at")
			.setTarget(bankHere)
		);
		ferry.addPatternEdge(new PatternEdge()
			.setAction("+")
			.setSource(ferry)
			.setName("at")
			.setTarget(bankThere)
		);
		bankHere.setAttributeMatchExpression("#{type} == 'Bank'");
		bankHere.addPatternEdge(new PatternEdge()
			.setSource(bankHere)
			.setName("opposite")
			.setTarget(bankThere)
		);
		bankThere.setAttributeMatchExpression("#{type} == 'Bank'");
		
		eater.setAttributeMatchExpression("#{type} == 'Cargo'");
		eater.addPatternEdge(new PatternEdge()
				.setSource(eater)
				.setName("eats")
				.setTarget(getsEaten));
		eater.addPatternEdge(new PatternEdge()
				.setSource(eater)
				.setName("at")
				.setTarget(bankHere));
		eater.setAction("!=");
		getsEaten.setAttributeMatchExpression("#{type} == 'Cargo'");
		getsEaten.addPatternEdge(new PatternEdge()
				.setSource(getsEaten)
				.setName("at")
				.setTarget(bankHere));
		getsEaten.setAction("!=");
		return pattern;
	}

	/**
	 * Method to obtain the (corrected) 'empty transport rule' of the ferryman's problem graph.
	 * Note: This rule doesn't allow the ferryman to leave two species alone, that eat each other!
	 * @return the (corrected) 'empty transport rule' of the ferryman's problem graph
	 */
	private PatternGraph getCorrectEmptyTranportRule() {
		PatternGraph pattern = new PatternGraph("correctEmptyTranportRule");
		PatternNode ferry = new PatternNode(), bankHere = new PatternNode(), bankThere = new PatternNode(), eater = new PatternNode(), getsEaten = new PatternNode();
		pattern.addPatternNode(ferry).addPatternNode(bankHere).addPatternNode(bankThere).addPatternNode(eater).addPatternNode(getsEaten);
		ferry.setAttributeMatchExpression("#{type} == 'Ferry'");
		ferry.addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankHere)
		);
		ferry.addPatternEdge(new PatternEdge()
				.setAction("+")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankThere)
		);
		bankHere.setAttributeMatchExpression("#{type} == 'Bank'");
		bankHere.addPatternEdge(new PatternEdge()
				.setSource(bankHere)
				.setName("opposite")
				.setTarget(bankThere)
		);
		bankThere.setAttributeMatchExpression("#{type} == 'Bank'");
		
		eater.setAttributeMatchExpression("#{type} == 'Cargo'");
		eater.addPatternEdge(new PatternEdge()
				.setSource(eater)
				.setName("eats")
				.setTarget(getsEaten));
		eater.addPatternEdge(new PatternEdge()
				.setSource(eater)
				.setName("at")
				.setTarget(bankHere));
		eater.setAction("!=");
		getsEaten.setAttributeMatchExpression("#{type} == 'Cargo'");
		getsEaten.addPatternEdge(new PatternEdge()
				.setSource(getsEaten)
				.setName("at")
				.setTarget(bankHere));
		getsEaten.setAction("!=");
		return pattern;
	}
	
	/**
	 * Method to obtain the initial situation of the ferryman's problem as a graph.
	 * @return the initial situation of the ferryman's problem as a graph.
	 */
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
	
	/**
	 * Method to obtain the solution of the ferryman's problem as a graph.
	 * @return the solution of the ferryman's problem as a graph.
	 */
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