package org.fujaba.graphengine;

import javax.xml.soap.Node;

import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.adapter.GraphAdapter;
import org.fujaba.graphengine.graph.adapter.NodeAdapter;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;
import org.fujaba.graphengine.pattern.adapter.PatternEdgeAdapter;
import org.fujaba.graphengine.pattern.adapter.PatternGraphAdapter;
import org.fujaba.graphengine.pattern.adapter.PatternNodeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GraphEngine {
	
	private static Gson gson;

	public static Gson getGson() {
		if (gson == null) {
			gson = new GsonBuilder()
					.registerTypeAdapter(Node.class, new NodeAdapter())
					.registerTypeAdapter(Graph.class, new GraphAdapter())
					.registerTypeAdapter(PatternEdge.class, new PatternEdgeAdapter())
					.registerTypeAdapter(PatternNode.class, new PatternNodeAdapter())
					.registerTypeAdapter(PatternGraph.class, new PatternGraphAdapter())
//					.setPrettyPrinting()
//					.serializeNulls()
					.create();
		}
		return gson;
	}
	
}
