package cfg;

import com.github.javaparser.ast.Node;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class CFGNode {

    @Getter
    private Node node;
    @Getter
    private List<Node> preNodes = new ArrayList<>();
    @Getter
    private List<Node> nextNodes = new ArrayList<>();

    public CFGNode(Node node) {
        this.node = node;
    }

    public String toString() {
        return this.node.toString();
    }

    public void addPreNode(Node node) {
        if (! this.preNodes.contains(node))
            this.preNodes.add(node);
    }

    public void addNextNode(Node node) {
        if (! this.nextNodes.contains(node))
            this.nextNodes.add(node);
    }

    public void removePreNode(Node node) {
        this.preNodes.remove(node);
    }

    public void removeNextNode(Node node) {
        this.nextNodes.remove(node);
    }

    public void clearNextNode() {
        this.nextNodes.clear();
    }

    public boolean equalNode(Node node) {
        return this.node == node;
    }

    public void mergeNode(CFGNode cfgNode) {
        this.preNodes.addAll(cfgNode.getPreNodes());
        this.nextNodes.addAll(cfgNode.getNextNodes());
    }

}
