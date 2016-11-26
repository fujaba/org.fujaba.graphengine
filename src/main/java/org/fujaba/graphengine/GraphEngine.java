package org.fujaba.graphengine;

import javax.xml.soap.Node;

import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.GraphAdapter;
import org.fujaba.graphengine.graph.NodeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GraphEngine {
	
	private static Gson gson;

	public static Gson getGson() {
		if (gson == null) {
			gson = new GsonBuilder()
					.registerTypeAdapter(Node.class, new NodeAdapter())
					.registerTypeAdapter(Graph.class, new GraphAdapter())
					.serializeNulls()
					.create();
		}
		return gson;
	}
	
}
