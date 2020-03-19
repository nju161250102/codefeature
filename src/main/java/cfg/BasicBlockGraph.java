package cfg;

import com.github.javaparser.ast.Node;

import java.util.*;
import java.util.stream.Collectors;

public class BasicBlockGraph {

    private List<String> basicBlocks = new ArrayList<>();
    private Map<Integer, Integer> edges = new HashMap<>();
    private int[][] adjacency;

    public BasicBlockGraph(CFG cfg) {
        int num = 0;
        int p = 0;
        List<BasicBlock> blockQueue = new ArrayList<>();
        blockQueue.add(new BasicBlock());
        List<Node> nodeQueue = new ArrayList<>();
        nodeQueue.add(cfg.getSourceNode());
        while (p < nodeQueue.size() && p < blockQueue.size()) {
            BasicBlock basicBlock = blockQueue.get(p);
            CFGNode node = cfg.findNode(nodeQueue.get(p));
            p ++;

            while (true) {
                int inDegree = node.getPreNodes().size() + (cfg.getSourceNode() == node.getNode() ? 1 : 0);
                int outDegree = node.getNextNodes().size() + (cfg.getSinkNodes().contains(node.getNode()) ? 1 : 0);

                if (inDegree == 1) {
                    basicBlock.addNode(node.getNode());
                } else {
                    BasicBlock preBlock = basicBlock;
                    int index = this.indexOfNode(nodeQueue, node.getNode());
                    if (index >= 0) {
                        basicBlock = blockQueue.get(index);
                        basicBlock.addNode(node.getNode());
                        preBlock.addNextBlock(basicBlock);
                    } else {
                        basicBlock = new BasicBlock();
                        basicBlock.addNode(node.getNode());
                        nodeQueue.add(node.getNode());
                        blockQueue.add(basicBlock);
                        preBlock.addNextBlock(basicBlock);
                    }
                }

                if (outDegree == 1) {
                    if (cfg.getSinkNodes().contains(node.getNode())) break;
                    node = cfg.findNode(node.getNextNodes().get(0));
                    if (this.indexOfNode(nodeQueue, node.getNode()) >= 0) {
                        BasicBlock nextBlock = blockQueue.get(this.indexOfNode(nodeQueue, node.getNode()));
                        basicBlock.addNextBlock(nextBlock);
                        break;
                    }
                } else {
                    for (Node n: node.getNextNodes()) {
                        if (this.indexOfNode(nodeQueue, n) < 0) {
                            nodeQueue.add(n);
                            BasicBlock newBlock = new BasicBlock();
                            basicBlock.addNextBlock(newBlock);
                            blockQueue.add(newBlock);
                        }
                    }
                    break;
                }
            }
        }
        this.basicBlocks = blockQueue.stream().map(BasicBlock::toString).collect(Collectors.toList());// 输出边
        for (int i = 0; i < blockQueue.size(); i++) {
            for (BasicBlock block: blockQueue.get(i).getNextBlocks()) {
                int j = blockQueue.indexOf(block);
                this.edges.put(i, j);
            }
        }

//        Queue<Node> queue = new LinkedList<>();
//        queue.offer(cfg.getSourceNode());
//        StringBuilder basicBlock = new StringBuilder();
//        while (queue.size() != 0) {
//            CFGNode node = cfg.findNode(queue.poll());
//            while (true) {
//                if (node.getNextNodes().size() > 1 || node.getPreNodes().size() > 1) {
//                    int nowNum = num;
//                    this.basicBlocks.add(basicBlock.toString());
//                    basicBlock = new StringBuilder();
//                    for (Node n: node.getNextNodes()) {
//                        boolean flag = true;
//                        for (Node n2: queue) {
//                            if (n == n2) flag = false;
//                        }
//                        if (flag) {
//                            queue.offer(n);
//                            num ++;
//                            this.edges.put(nowNum, num);
//                        }
//                    }
//                    break;
//                } else if (node.getNextNodes().size() == 1) {
//                    basicBlock.append(node.toString());
//                    basicBlock.append("\n");
//                    node = cfg.findNode(node.getNextNodes().get(0));
//                } else {
//                    break;
//                }
//            }
//
//        }
    }

    public String toDot() {
        StringBuilder builder = new StringBuilder("digraph cfg {\n").append("node[shape=box];\n");
        // 输出节点
        for (int i = 0; i < this.basicBlocks.size(); i++) {
            String label = this.basicBlocks.get(i).toString();
            label = label.replace("\n", "\\l");
            label = label.replace("\"", "\\\"");
            builder.append(String.format("n%d[label=\"%s\"];\n", i, label));
        }
        // 输出边
        this.edges.forEach((from, to) -> {
            builder.append(String.format("n%d->n%d;\n", from, to));
        });
        // 结束
        builder.append("}\n");
        return builder.toString();
    }

    private int indexOfNode(List<Node> nodeList, Node node) {
        for (int i = 0; i < nodeList.size(); i++) {
            if (node == nodeList.get(i)) return i;
        }
        return -1;
    }
}
