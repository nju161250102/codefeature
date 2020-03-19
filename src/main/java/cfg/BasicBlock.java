package cfg;

import com.github.javaparser.ast.Node;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {

    private List<Node> nodeList = new ArrayList<>();
    private BasicBlock preBlock;
    @Getter
    private List<BasicBlock> nextBlocks = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Node node: this.nodeList) {
            builder.append(node.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    Node getFirstNode() {
        if (this.nodeList.size() > 0) {
            return this.nodeList.get(0);
        } else {
            return null;
        }
    }

    Node getLastNode() {
        if (this.nodeList.size() > 0) {
            return this.nodeList.get(this.nodeList.size() - 1);
        } else {
            return null;
        }
    }

    void addNode(Node node) {
        this.nodeList.add(node);
    }

    void addNextBlock(BasicBlock block) {
        this.nextBlocks.add(block);
    }
}
