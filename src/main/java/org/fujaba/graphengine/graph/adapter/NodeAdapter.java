package org.fujaba.graphengine.graph.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.fujaba.graphengine.IdManager;
import org.fujaba.graphengine.graph.Node;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The NodeAdapter is a gson TypeAdapter used to serialize and deserialize Nodes to and from JSON. Though this Object doesn't make sense to serialize without its parent context - so it is prohibited by this Adapter.
 * 
 * @author Philipp Kolodziej
 */
public class NodeAdapter extends TypeAdapter<Node>
{

   @Override
   public void write(JsonWriter out, Node node) throws IOException
   {
      IdManager idManager = new IdManager();
      out.beginObject();
      // the part for the node begins
      out.name("id").value(idManager.getId(node));
      out.name("attributes");
      out.beginObject();
      ArrayList<String> keys = new ArrayList<String>(node.getAttributes().keySet());
      Collections.sort(keys);
      for (String key : keys)
      {
         Object value = node.getAttribute(key);
         if (value instanceof Integer)
         {
            out.name(key).value((Integer) node.getAttribute(key));
         }
         else if (value instanceof Long)
         {
            out.name(key).value((Long) node.getAttribute(key));
         }
         else if (value instanceof Double)
         {
            out.name(key).value((Double) node.getAttribute(key));
         }
         else if (value instanceof Boolean)
         {
            out.name(key).value((Boolean) node.getAttribute(key));
         }
         else if (value instanceof String)
         {
            out.name(key).value((String) node.getAttribute(key));
            // } else if (value == null) {
            // out.name(key).value((Integer)null);
         }
         else
         {
            throw new IOException("invalid type of attribute value for key " + key + " in Node " + node + ": " + value);
         }
      }
      out.endObject();
      out.name("edges");
      out.beginArray();
      keys = new ArrayList<String>(node.getEdges().keySet());
      Collections.sort(keys);
      for (String key : keys)
      {
         out.beginObject();
         out.name(key);
         out.beginArray();
         for (Node targetNode : node.getEdges(key))
         {
            out.value(idManager.getId(targetNode));
         }
         out.endArray();
         out.endObject();
      }
      out.endArray();
      out.endObject();
   }


   @Override
   public Node read(JsonReader in) throws IOException
   {
      throw new IOException("single Nodes cannot be read. try reading the whole Graph!");
   }

}
