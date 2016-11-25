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
			final GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(Node.class, new NodeAdapter());
			gsonBuilder.registerTypeAdapter(Graph.class, new GraphAdapter());
			gson = gsonBuilder.create();
		}
		return gson;
	}
	
}
