package cfg;

import com.github.javaparser.ast.Node;

import java.util.*;
import java.util.stream.Collectors;

public class CFG {

    private List<Node> sourceNodes = new LinkedList<>();
    private List<Node> sinkNodes = new LinkedList<>();
    private Map<Node, List<Node>> map = new HashMap<>();

    public CFG() {
//        this.start = new CFGNode(null);
    }

//    private List<Node> findSource() {
//        List<Node> result = new LinkedList<>();
//        for (Node node : this.map.keySet()) {
//            if (this.map.values().stream().flatMap(Collection::stream).filter(n -> {return (n == node);}).collect(Collectors.toList()).isEmpty())
//                result.add(node);
//        }
//        return result;
//    }
//
//    private List<Node> findSink() {
//        List<Node> result = new LinkedList<>();
//        for (Node node : this.map.keySet()) {
//            if (this.map.get(node).isEmpty()) {
//                result.add(node);
//            }
//        }
//        return result;
//    }

    public Map<Node, List<Node>> getMap() {
        return this.map;
    }

    public void setSourceNodes(Node... nodes) {
        this.sourceNodes = new LinkedList<>(Arrays.asList(nodes));
    }

    public void setSourceNodes(Collection nodes) {
        this.sourceNodes = new LinkedList<>();
        this.sourceNodes.addAll(nodes);
    }

    public void setSinkNodes(Node... nodes) {
        this.sinkNodes = new LinkedList<>(Arrays.asList(nodes));
    }

    public void setSinkNodes(Collection nodes) {
        this.sinkNodes = new LinkedList<>();
        this.sinkNodes.addAll(nodes);
    }

    public List<Node> getSourceNodes() {
        return this.sourceNodes;
    }

    public List<Node> getSinkNodes() {
        return this.sinkNodes;
    }

    public void addNode(Node node) {
        this.map.put(node, new LinkedList<>());
    }

    public void addEdge(Node fromNode, Node toNode) {
        if (! this.map.containsKey(fromNode)) {
            this.addNode(fromNode);
        }
        if (! this.map.containsKey(toNode)) {
            this.addNode(toNode);
        }
        this.map.get(fromNode).add(toNode);
    }

    public List<Node> findPreNodes(Node node) {
        List<Node> result = new LinkedList<>();
        for (Map.Entry<Node, List<Node>> entry : this.map.entrySet()) {
            if (entry.getValue().contains(node)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public List<Node> findNextNodes(Node node) {
        if (this.map.keySet().contains(node)) {
            return this.map.get(node);
        }
        return new LinkedList<>();
    }

    public void deleteEdge(Node fromNode, Node toNode) {
        if (this.map.containsKey(fromNode)) {
            this.map.get(fromNode).remove(toNode);
        }
    }

    public void mergeCFG(Node node, CFG cfg) {
        if (this.sourceNodes.remove(node)) {
            this.sourceNodes.addAll(cfg.getSourceNodes());
        }
        if (this.sinkNodes.remove(node)) {
            this.sinkNodes.addAll(cfg.getSinkNodes());
        }
        for (Node n1: this.findPreNodes(node)) {
            for (Node n2: cfg.getSourceNodes()) {
                this.addEdge(n1, n2);
            }
            this.deleteEdge(n1, node);
        }
        for (Node n1: this.findNextNodes(node)) {
            for (Node n2: cfg.getSinkNodes()) {
                this.addEdge(n2, n1);
            }
            this.deleteEdge(node, n1);
        }
        this.map.remove(node);
        for (Map.Entry<Node, List<Node>> entry : cfg.getMap().entrySet()) {
            if (this.map.keySet().contains(entry.getKey())) {
                this.map.get(entry.getKey()).addAll(entry.getValue());
            } else {
                this.map.put(entry.getKey(), entry.getValue());
            }
        }
//        System.out.println(this.toDot());
    }

    public String toDot() {
        StringBuilder buffer = new StringBuilder("digraph cfg {\n").append("node[shape=box];\n");
        List<Node> nodeList = new ArrayList<>(this.map.keySet());
        for (int i = 0; i < nodeList.size(); i++) {
            String label = nodeList.get(i).toString();
            label = label.replace("\n", "\\l");
            buffer.append("n").append(i).append("[label=\"").append(label).append("\"];\n");
        }
        for (int i = 0; i < nodeList.size(); i++) {
            List<Node> neighbours = this.map.get(nodeList.get(i));
            for (Node node: neighbours) {
                int j = nodeList.indexOf(node);
                buffer.append("n").append(i).append(" -> ").append("n").append(j).append(";\n");
            }
        }
        buffer.append("}");
        return buffer.toString();
    }

}
