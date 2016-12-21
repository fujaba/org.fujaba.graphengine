package org.fujaba.graphengine;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.graph.adapter.GraphAdapter;
import org.fujaba.graphengine.graph.adapter.GraphToSigmaJsAdapter;
import org.fujaba.graphengine.graph.adapter.NodeAdapter;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandler;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandlerCSPHighHeuristics;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandlerCSPLowHeuristics;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandlerSorting;
import org.fujaba.graphengine.isomorphismtools.sort.NodeSortTree;
import org.fujaba.graphengine.isomorphismtools.sort.adapter.NodeSortTreeAdapter;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;
import org.fujaba.graphengine.pattern.adapter.PatternEdgeAdapter;
import org.fujaba.graphengine.pattern.adapter.PatternGraphAdapter;
import org.fujaba.graphengine.pattern.adapter.PatternNodeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The GraphEngine is a class that contains methods to handle graphs.
 * 
 * @author Philipp Kolodziej
 */
public class GraphEngine {
	
	private static Gson gson;
	private static Gson gsonForSigmaJs;
	private static IsomorphismHandler mainIsomorphismHandler;
	private static IsomorphismHandler mappingFallback;
	private static IsomorphismHandler normalizationFallback;
	private static IsomorphismHandler splitGraphFallback;

	public static void setMainIsomorphismHandler(IsomorphismHandler isomorphismHandler) {
		if (isomorphismHandler != null) {
			mainIsomorphismHandler = isomorphismHandler;
		}
	}
	/**
	 * Returns the current main IsomorphismHandler, that's used for all isomorphism checks, that it does support
	 * @return the current main IsomorphismHandler, that's used for all isomorphism checks, that it does support
	 */
	public static IsomorphismHandler getMainIsomorphismHandler() {
		if (mainIsomorphismHandler == null) {
			mainIsomorphismHandler = new IsomorphismHandlerCSPLowHeuristics();
		}
		return mainIsomorphismHandler;
	}
	/**
	 * Returns an IsomorphismHandler as fallback for an otherwise unimplemented/not functioning mappingFrom-Function
	 * @return an IsomorphismHandler as fallback for an otherwise unimplemented/not functioning mappingFrom-Function
	 */
	public static IsomorphismHandler getMappingFallback() {
		if (mappingFallback == null) {
			mappingFallback = new IsomorphismHandlerCSPLowHeuristics();
		}
		return mappingFallback;
	}
	/**
	 * Returns an IsomorphismHandler as fallback for an otherwise unimplemented/not functioning normalized-Function
	 * @return an IsomorphismHandler as fallback for an otherwise unimplemented/not functioning normalized-Function
	 */
	public static IsomorphismHandler getNormalizationFallback() {
		if (normalizationFallback == null) {
			normalizationFallback = new IsomorphismHandlerSorting();
		}
		return normalizationFallback;
	}
	/**
	 * Returns an IsomorphismHandler as fallback for an otherwise unimplemented/not functioning handling of split graphs
	 * @return an IsomorphismHandler as fallback for an otherwise unimplemented/not functioning handling of split graphs
	 */
	public static IsomorphismHandler getSplitGraphFallback() {
		if (splitGraphFallback == null) {
			splitGraphFallback = new IsomorphismHandlerCSPLowHeuristics();
		}
		return splitGraphFallback;
	}

	/**
	 * Getter for the GraphEngine's gson
	 * 
	 * @return a gson-Object with the necessary custom TypeAdapters.
	 */
	public static Gson getGson() {
		if (gson == null) {
			gson = new GsonBuilder()
					.registerTypeAdapter(Node.class, new NodeAdapter())
					.registerTypeAdapter(Graph.class, new GraphAdapter())
					.registerTypeAdapter(PatternEdge.class, new PatternEdgeAdapter())
					.registerTypeAdapter(PatternNode.class, new PatternNodeAdapter())
					.registerTypeAdapter(PatternGraph.class, new PatternGraphAdapter())
					.registerTypeAdapter(NodeSortTree.class, new NodeSortTreeAdapter())
//					.setPrettyPrinting()
//					.serializeNulls()
					.create();
		}
		return gson;
	}

	/**
	 * Getter for the GraphEngine's gson
	 * 
	 * @return a gson-Object with the necessary custom TypeAdapters.
	 */
	public static Gson getGsonForSigmaJs() {
		if (gsonForSigmaJs == null) {
			gsonForSigmaJs = new GsonBuilder()
					.registerTypeAdapter(Graph.class, new GraphToSigmaJsAdapter())
//					.setPrettyPrinting()
//					.serializeNulls()
				.create();
		}
		return gsonForSigmaJs;
	}
	
	public static void prepareGraphAsJsonFileForSigmaJs(Graph graph) {
		prepareGraphAsJsonFileForSigmaJs(graph, "data.json");
	}
	
	public static void prepareGraphAsJsonFileForSigmaJs(Graph graph, String filename) {
		try (Writer writer = new FileWriter("src/main/resources/" + filename)) {
			getGsonForSigmaJs().toJson(graph, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This function checks for this graph and a given sub-graph,
	 * if the sub-graph is isomorph to a sub-graph of this graph and returns the mapping.
	 * 
	 * @param graph the given base-graph
	 * @param subGraph the given sub-graph
	 * @return a mapping from the given sub-graph to nodes of this graph if possible, or null
	 */
	public static HashMap<Node, Node> mappingFrom(Graph subGraph, Graph baseGraph) {
		return getMainIsomorphismHandler().mappingFrom(subGraph, baseGraph);
	}
	public static boolean isIsomorphicSubGraph(Graph subGraph, Graph baseGraph) {
		return getMainIsomorphismHandler().isIsomorphicSubGraph(subGraph, baseGraph);
	}
	
	/**
	 * This function returns true if the other graph is isomorph to this graph.
	 * 
	 * @param other the other graph
	 * @return true if the graphs are isomorph
	 */
	public static boolean isIsomorphTo(Graph one, Graph other) {
		return getMainIsomorphismHandler().isIsomorphTo(one, other);
	}

	/**
	 * Returns all separate graphs (parts of this graph with no edges in between) as new graphs.
	 * @param graph the graph to split
	 * @return all separate graphs (parts of this graph with no edges in between) as new graphs
	 */
	public static ArrayList<Graph> split(Graph graph) {
		return split(graph, false);
	}
	/**
	 * Returns all separate graphs (parts of this graph with no edges in between) as new graphs.
	 * @param graph the graph to split
	 * @param keepGraph whether to keep the graph-nodes (true) or to use a clone (false)
	 * @return all separate graphs (parts of this graph with no edges in between) as new graphs
	 */
	public static ArrayList<Graph> split(Graph graph, boolean keepGraph) {
		ArrayList<Graph> result = new ArrayList<Graph>();
		if (graph.getNodes().size() <= 1) {
			result.add(graph.clone());
			return result;
		}
		int count = 0;
		Graph clone = graph;
		if (!keepGraph) {
			clone = clone.clone();
		}
		while (count < graph.getNodes().size()) {
			Graph subGraph = new Graph();
			ArrayList<Node> subGraphNodes = connectedNodes(clone, clone.getNodes().get(0));
			clone.getNodes().removeAll(subGraphNodes);
			subGraph.getNodes().addAll(subGraphNodes);
			result.add(subGraph);
			count += subGraphNodes.size();
		}
		clone.getNodes().clear();
		for (int i = 0; i < result.size(); ++i) {
			clone.getNodes().addAll(result.get(i).getNodes());
		}
		return result;
	}
	
	/**
	 * Returns true if there are no separate graphs (parts of the graph with no edges in between). 
	 * @return true if there are no separate graphs (parts of the graph with no edges in between), otherwise false.
	 */
	public static boolean isConnected(Graph graph) {
		if (graph.getNodes().size() <= 1) {
			return true;
		}
		return connectedNodes(graph, graph.getNodes().get(0)).size() == graph.getNodes().size();
	}
	
	/**
	 * This function basically does a search for all nodes connected to the given node and returns them in an ArrayList<Node>
	 * @param node the node to do the check with
	 * @return all nodes connected to the given node in an ArrayList<Node>
	 */
	private static ArrayList<Node> connectedNodes(Graph graph, Node node) {
		ArrayList<Node> open = new ArrayList<Node>();
		ArrayList<Node> closed = new ArrayList<Node>();
		if (!graph.getNodes().contains(node)) {
			return closed;
		}
		open.add(node);
		while (open.size() > 0) {
			Node current = open.remove(0);
			closed.add(current);
			ArrayList<Node> succ = new ArrayList<Node>();
			for (String edgeName: current.getEdges().keySet()) {
				for (Node outgoing: current.getEdges(edgeName)) {
					if (!open.contains(outgoing) && !closed.contains(outgoing) && !succ.contains(outgoing)) {
						succ.add(outgoing);
					}
				}
			}
			for (Node ingoing: graph.getNodes()) {
				for (String edgeName: ingoing.getEdges().keySet()) {
					if (ingoing.getEdges(edgeName) != null && ingoing.getEdges(edgeName).contains(current)
							&& !open.contains(ingoing) && !closed.contains(ingoing) && !succ.contains(ingoing)) {
						succ.add(ingoing);
						break;
					}
				}
			}
			open.addAll(succ);
		}
		return closed;
	}
	
}
