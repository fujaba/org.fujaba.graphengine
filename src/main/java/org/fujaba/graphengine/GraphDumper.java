package org.fujaba.graphengine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;

/**
 * This dumper is heavily inspired by Christoph Eickhoff's master thesis' graph dumper.
 * 
 * @author Philipp Kolodziej
 */
public class GraphDumper {
	
	private Graph graph;
	
	public GraphDumper(Graph graph) {
		this.graph = graph;
	}
	
	public void dumpGraph(String targetFileName) {
		try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(targetFileName));
			ArrayList<Node> states = graph.getNodes();
			ArrayList<Node> edgeSources = new ArrayList<Node>();
			ArrayList<String> edgeNames = new ArrayList<String>();
			ArrayList<Node> edgeTargets = new ArrayList<Node>();
			writer.write(ALCHEMY_START.replaceAll("<<folder>>", "../"));
			writer.write("\nvar data= {\n" +
			"  \"nodes\": [\n");
			for (int i = 0; i < states.size(); ++i) {
				Node currentNode = states.get(i);
				writer.write('{');
				if (currentNode.getEdges().keySet().size() == 0) {
					writer.write("type:\"finalstate\",");
				} else if (i == 0) {
					writer.write("type:\"startstate\",");
				} else {
					writer.write("type:\"state\",");
				}
				writer.write("id:" + i + "");
				writer.write(",caption:\"state\"");
				writer.write('}');
				for (String key: currentNode.getEdges().keySet()) {
					for (Node target: currentNode.getEdges(key)) {
						edgeSources.add(currentNode);
						edgeNames.add("edge"); // TODO
//						edgeNames.add(key); // TODO
						edgeTargets.add(target);
					}
				}
				if (i < states.size() - 1) {
					writer.write(',');
				}
			}
			writer.write("],\n    \"edges\": [");
			HashSet<String> edgeStrings = new HashSet<>();
			boolean first = true;
			for (int i = 0; i < edgeTargets.size(); ++i) {
				if (states.indexOf(edgeSources.get(i)) == -1 || states.indexOf(edgeTargets.get(i)) == -1) {
					continue;
				}
				String source = "source:" + states.indexOf(edgeSources.get(i)) + ",";
				String target = "target:" + states.indexOf(edgeTargets.get(i)) + ",";
				String edgestring = source + target;
				if (!edgeStrings.contains(edgestring)) {
					if (!first) {
						writer.write(',');
					}
					first = false;
					edgeStrings.add(edgestring);
					writer.write('{');
					writer.write(source);
					writer.write(target);
					writer.write("nr:" + i + ",");
					writer.write("caption:\"" + edgeNames.get(i) + "\",");
					writer.write("type:\"normal\"");
					writer.write("}");
				}
			}
			writer.write(" ]\n" +
			"}\n");
			writer.write(ALCHEMY_CONFIG_STATEGRAPH);
			writer.write(ALCHEMY_END);
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	// https://cdnjs.cloudflare.com/ajax/libs/alchemyjs/0.4.2/alchemy.css
	// https://cdnjs.cloudflare.com/ajax/libs/alchemyjs/0.4.2/styles/vendor.css
	// https://cdnjs.cloudflare.com/ajax/libs/alchemyjs/0.4.2/scripts/vendor.js
	// https://cdnjs.cloudflare.com/ajax/libs/alchemyjs/0.4.2/alchemy.js
	
    private static final String ALCHEMY_START = "<html>\n" +
		"<head>\n" +
		" <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/alchemyjs/0.4.2/alchemy.css\">\n" +
		" <link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdnjs.cloudflare.com/ajax/libs/alchemyjs/0.4.2/styles/vendor.css\">\n" +
		"<script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/alchemyjs/0.4.2/scripts/vendor.js\"></script>\n" +
		"<script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/alchemyjs/0.4.2/alchemy.js\"></script>\n" +
		"\n" +
		"</head>\n" +
		"<body>\n" +
		"\n" +
		"  <div class=\"alchemy\" id=\"alchemy\"></div>\n" +
		"  <script type=\"text/javascript\">";

    private static final String ALCHEMY_CONFIG_STATEGRAPH = "   var config = {\n" + "            dataSource: data,\n" +
		"        directedEdges:true,\n" +
		"        initialScale: 0.16, \n" +
		"        initialTranslate: [750,420],\n" +
		"        curvedEdges:true,\n" +
		"        edgeCaptionsOnByDefault: true,\n" +
		"        nodeTypes: {\"type\":[\"failure\",\"state\",\"startstate\",\"finalstate\"]},\n" +
		"         \"nodeStyle\": {\n" +
		"         \"failure\": {\n" +
		"               color: \"#FE3E3E\",\n" +
		"            },\n" +
		"         \"finalstate\": {\n" +
		"               color: \"#0000FF\",\n" +
		"               radius: 15, \n" +
		"            borderColor: \"#127DC1\"" +
		"            },\n" +
		"         \"startstate\": {\n" +
		"               color: \"#FFFFFF\",\n" +
		"               radius: 15, \n" +
		"            borderColor: \"#127DC1\"" +
		"            },\n" +
		"         \"state\":{\n" +
		"               color: \"#00ff0e\",\n" +
		"               borderColor: \"#00ffda\"\n" +
		"         }\n" +
		"      },\n" +
		"          \"edgeTypes\": {\"type\": [\"failure\",\"normal\"]},\n" +
		"      \"edgeStyle\": {\n" +
		"            \"failure\": {\n" +
		"               color: \"#ff794d\",\n" +
		"            width: 7,\n" +
		"               opacity: 1\n" +
		"         },\n" +
		"            \"greenpath\": {\n" +
		"               color: \"#00FF00\",\n" +
		"            width: 7,\n" +
		"               opacity: 1\n" +
		"         },\n" +
		"         \"normal\": {\n" +
		"            opacity: .9\n" +
		"         }\n" +
		"      }" +
		",     \n" +
		"       nodeClick: function OpenInNewTab(node) {\n" +
		"               var win = window.open('objectgraphs/graph'+node.id+'.html', '_blank');\n" +
		"               if (win) {\n" +
		"                     win.focus();\n" +
		"               } else {\n" +
		"                     alert('Please allow popups for this website');\n" +
		"               }\n" +
		"            }     \n" +
		",     \n" +
		"       edgeClick: function OpenInNewTab(edge) {\n" +
		"               var win = window.open('rulegraphs/graph'+edge._properties.nr+'.html', '_blank');\n" +
		"               if (win) {\n" +
		"                     win.focus();\n" +
		"               } else {\n" +
		"                     alert('Please allow popups for this website');\n" +
		"               }\n" +
		"            }     \n" +
		"    };\n";

//    private static final String ALCHEMY_CONFIG_GRAPHROOTS = "   var config = {\n" + "            dataSource: data,\n" +
//		"        directedEdges:true,\n" +
//		"        initialScale: 0.7, \n" +
//		"        initialTranslate: [250,150],\n" +
//		"        nodeCaptionsOnByDefault: true,\n" +
//		"        edgeCaptionsOnByDefault: true,\n" +
//		"        nodeTypes: {\"type\":[\"failure\",\"state\"]},\n" +
//		"         \"nodeStyle\": {\n" +
//		"         \"failure\": {\n" +
//		"               color: \"#FE3E3E\",\n" +
//		"            },\n" +
//		"         \"state\":{\n" +
//		"               color: \"#00ff0e\",\n" +
//		"               borderColor: \"#00ffda\"\n" +
//		"         }\n" +
//		"      },\n" +
//		"          \"edgeTypes\": {\"type\": [\"failure\",\"normal\"]},\n" +
//		"      \"edgeStyle\": {\n" +
//		"            \"failure\": {\n" +
//		"               color: \"#ff794d\",\n" +
//		"            width: 7,\n" +
//		"               opacity: 1\n" +
//		"         },\n" +
//		"         \"normal\": {\n" +
//		"            \n" +
//		"         }\n" +
//		"      },     \n"
//		  + " nodeMouseOver: function(n) {\n" +
//		"if(n._properties.vars === \"\") {\n" +
//		"       n._properties.caption =\"<<no vars>>\"" +
//		"}else{" +
//		"        n._properties.caption =  n._properties.vars;\n" +
//		"}" +
//		"         n.setStyles();\n" +
//		"      }," +
//		" nodeMouseOut: function(o) {\n" +
//		"        var n;\n" +
//		"        n = o.self;\n" +
//		"        n._properties.caption =  n._properties.name;\n" +
//		"         n.setStyles();\n" +
//		"      }" +
//		"    };\n";
//
//    private static final String ALCHEMY_CONFIG_RULES = "   var config = {\n" + "            dataSource: data,\n" +
//		"        directedEdges:true,\n" +
//		"        initialScale: 0.7, \n" +
//		"        initialTranslate: [250,150],\n" +
//		"        nodeCaptionsOnByDefault: true,\n" +
//		"        edgeCaptionsOnByDefault: true,\n" +
//		"        nodeTypes: {\"type\":[\"failure\",\"state\"]},\n" +
//		"         \"nodeStyle\": {\n" +
//		"         \"failure\": {\n" +
//		"               color: \"#FE3E3E\",\n" +
//		"            },\n" +
//		"         \"state\":{\n" +
//		"               color: \"#00ff0e\",\n" +
//		"               borderColor: \"#00ffda\"\n" +
//		"         }\n" +
//		"      },\n" +
//		"          \"edgeTypes\": {\"type\": ["
//		  + "\"failure\",\"normal\""
//		  + ",\"create\",\"destroy\""
//		  + ",\"tgtMatch\",\"srcMatch\"]},\n" +
//		"      \"edgeStyle\": {\n" +
//		"            \"failure\": {\n" +
//		"               color: \"#ff0000\",\n" +
//		"            width: 7,\n" +
//		"               opacity: 1\n" +
//		"         },\n" +
//		"            \"create\": {\n" +
//		"               color: \"#00ff00\",\n" +
//		"            width: 4,\n" +
//		"               opacity: 0.8\n" +
//		"         },\n" +
//		"            \"destroy\": {\n" +
//		"               color: \"#ff0000\",\n" +
//		"            width: 4,\n" +
//		"               opacity: 0.8\n" +
//		"         },\n" +
//		"         \"normal\": {\n" +
//		"            \n" +
//		"         },\n" +
//		"         \"tgtMatch\": {\n" +
//		"               color: \"#993333\",\n" +
//		"                width: 1,\n" +
//		"               opacity: 1\n" +
//		"         },\n" +
//		"         \"srcMatch\": {\n" +
//		"               color: \"#FF5500\",\n" +
//		"                width: 1,\n" +
//		"               opacity: 1\n" +
//		"         }"
//		  + "}\n"
//		  + ", cluster: true,\n" +
//		"            clusterColours: [\"#1B9E77\",\"#D95F02\",\"#7570B3\",\"#E7298A\",\"#66A61E\",\"#E6AB02\"]   \n" +
//		"    };\n";

    private static final String ALCHEMY_END = "\n" +
		"        alchemy = new Alchemy(config)\n" +
		"    </script>\n" +
		"  </body>\n" +
		"</html>";

}
