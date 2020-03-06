package cfg;

import com.github.javaparser.ast.Node;

import java.util.List;

public class CFGNode {

    private Node node;
    private String code;
    private List<CFGNode> children;

    public CFGNode(Node node) {
        this.node = node;
        this.code = node.toString();
    }

    public void addNode(CFGNode cfgNode) {
        this.children.add(cfgNode);
    }

}
