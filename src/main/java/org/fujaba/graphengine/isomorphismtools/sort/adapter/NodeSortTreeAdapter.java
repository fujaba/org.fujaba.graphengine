package org.fujaba.graphengine.isomorphismtools.sort.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.fujaba.graphengine.IdManager;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.isomorphismtools.sort.NodeSortTree;
import org.fujaba.graphengine.isomorphismtools.sort.NodeSortTreeNode;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class NodeSortTreeAdapter extends TypeAdapter<NodeSortTree> {

	@Override
	public void write(JsonWriter out, NodeSortTree value) throws IOException {
		IdManager idManager = new IdManager();
		write(out, value.getRootNodeSortTreeNode(), idManager);
	}
	
	private void write(JsonWriter out, NodeSortTreeNode nodeSortTreeNode, IdManager idManager) throws IOException {
		out.beginObject();
		Node node = nodeSortTreeNode.getNode();
		out.name("id").value(idManager.getId(node));
		out.name("attributs").beginObject();
		ArrayList<String> keys = new ArrayList<String>(node.getAttributes().keySet());
		Collections.sort(keys);
		for (String key: keys) {
			Object value = node.getAttribute(key);
			if (value instanceof Boolean) {
				out.name(key).value((Boolean)value);
			} else if (value instanceof Integer) {
				out.name(key).value((Integer)value);
			} else if (value instanceof Long) {
				out.name(key).value((Long)value);
			} else if (value instanceof Double) {
				out.name(key).value((Double)value);
			} else {
				out.name(key).value((String)value);
			}
		}
		out.endObject();
		out.name("edges").beginArray();
		keys = new ArrayList<String>(node.getEdges().keySet());
		Collections.sort(keys);
		for (String key: keys) {
			out.beginObject();
			out.name(key).beginArray();
			for (Node target: node.getEdges(key)) {
				out.value(idManager.getId(target));
			}
			out.endArray();
			out.endObject();
		}
		out.endArray();
		out.name("children").beginArray();
		for (NodeSortTreeNode childNodeSortTreeNode: nodeSortTreeNode.getChildrenNodeSortTreeNodes()) {
			write(out, childNodeSortTreeNode, idManager);
		}
		out.endArray();
		out.endObject();
	}

	@Override
	public NodeSortTree read(JsonReader in) throws IOException {
		throw new IOException("Don't try to read in NodeSortTrees, they are made for serialization only!");
	}
	
	

}
