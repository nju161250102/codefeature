package cfg;

import com.github.javaparser.ast.Node;
import lombok.Getter;

import java.util.*;

public class CFG {

    @Getter
    private List<Node> sourceNodes = new LinkedList<>();
    @Getter
    private List<Node> sinkNodes = new LinkedList<>();
    @Getter
    private List<CFGNode> vertexList = new ArrayList<>();

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

    public CFGNode findNode(Node node) {
        for (CFGNode v: this.vertexList) {
            if (v.equalNode(node)) {
                return v;
            }
        }
        return null;
    }

    public boolean containNode(Node node) {
        return this.findNode(node) != null;
    }

    public void addNode(Node node) {
        if (! this.containNode(node)) {
            this.vertexList.add(new CFGNode(node));
        }
    }

    public void addEdge(Node fromNode, Node toNode) {
        if (! this.containNode(fromNode)) {
            this.vertexList.add(new CFGNode(fromNode));
        }
        if (! this.containNode(toNode)) {
            this.vertexList.add(new CFGNode(toNode));
        }
        CFGNode v1 = this.findNode(fromNode);
        CFGNode v2 = this.findNode(toNode);
        v1.addNextNode(toNode);
        v2.addPreNode(fromNode);
    }

    public List<Node> findPreNodes(Node node) {
        CFGNode cfgNode = this.findNode(node);
        return cfgNode != null ? cfgNode.getPreNodes() : new ArrayList<>();
    }

    public List<Node> findNextNodes(Node node) {
        CFGNode cfgNode = this.findNode(node);
        return cfgNode != null ? cfgNode.getNextNodes() : new ArrayList<>();
    }

    public void deleteEdge(Node fromNode, Node toNode) {
        CFGNode v1 = this.findNode(fromNode);
        CFGNode v2 = this.findNode(toNode);
        if (v1 != null && v2 != null) {
            v1.removeNextNode(toNode);
            v2.removePreNode(fromNode);
        }
    }

    public void removeNode(Node node) {
        CFGNode cfgNode = this.findNode(node);
        this.vertexList.remove(cfgNode);
        for (CFGNode v: this.vertexList) {
            if (v.getPreNodes().contains(node))
                v.removePreNode(node);
            if (v.getNextNodes().contains(node))
                v.removeNextNode(node);
        }
    }

    public void mergeCFG(Node node, CFG cfg) {
        CFGNode cfgNode = this.findNode(node);
        if (cfgNode == null) return;

        if (this.sourceNodes.remove(node)) {
            this.sourceNodes.addAll(cfg.getSourceNodes());
        }
        if (this.sinkNodes.remove(node)) {
            this.sinkNodes.addAll(cfg.getSinkNodes());
        }

        for (Node n1: cfgNode.getPreNodes()) {
            for (Node n2: cfg.getSourceNodes()) {
                this.addEdge(n1, n2);
            }
        }
        for (Node n1: cfgNode.getNextNodes()) {
            for (Node n2: cfg.getSinkNodes()) {
                this.addEdge(n2, n1);
            }
        }

        this.removeNode(node);
        for (CFGNode v1: cfg.getVertexList()) {
            if (this.containNode(v1.getNode())) {
                CFGNode v2 = this.findNode(v1.getNode());
                v2.mergeNode(v1);
            } else {
                this.vertexList.add(v1);
            }
        }
//        System.out.println(this.toDot());
    }

    public String toDot() {
        StringBuilder buffer = new StringBuilder("digraph cfg {\n").append("node[shape=box];\n");
        for (int i = 0; i < this.vertexList.size(); i++) {
            String label = this.vertexList.get(i).toString();
            label = label.replace("\n", "\\l");
            label = label.replace("\"", "\\\"");
            buffer.append(String.format("n%d[label=\"%s\"];\n", i, label));
        }
        for (int i = 0; i < this.vertexList.size(); i++) {
            for (Node node: this.vertexList.get(i).getNextNodes()) {
                int j = this.vertexList.indexOf(this.findNode(node));
                buffer.append(String.format("n%d->n%d;\n", i, j));
            }
        }
        buffer.append("start[label=\"Start\"];\n");
        buffer.append("end[label=\"End\"];\n");
        for (Node node: this.sourceNodes) {
            int index = this.vertexList.indexOf(this.findNode(node));
            buffer.append(String.format("start->n%d;\n", index));
        }
        for (Node node: this.sinkNodes) {
            int index = this.vertexList.indexOf(this.findNode(node));
            buffer.append(String.format("n%d->end;\n", index));
        }
        buffer.append("}\n");
        return buffer.toString();
    }

}
