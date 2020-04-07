package deepwalk;

import cfg.BasicBlock;
import cfg.BasicBlockGraph;
import org.deeplearning4j.graph.graph.Graph;
import org.deeplearning4j.graph.models.deepwalk.DeepWalk;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GraphFeature {

    public static List<List<Double>> extract(BasicBlockGraph basicBlockGraph) {
        // 参数设置
        int vectorSize = 16;
        int windowSize = 2;
        double learningRate = 0.001;

        List<List<Double>> result = new ArrayList<>();
        if (basicBlockGraph.getBasicBlocks().size() < 2) return result;

        List<String> vertexList = basicBlockGraph.getBasicBlocks().stream().map(BasicBlock::toString).collect(Collectors.toList());
        MyVertexFactory<String> vertexFactory = new MyVertexFactory<>(vertexList);
        Graph<String,String> graph = new Graph<>(vertexList.size(), vertexFactory);

        List<List<Integer>> edges = basicBlockGraph.getEdges();
        for( int i = 0; i < edges.size(); i++ ){
            for (Integer n: edges.get(i)) {
                graph.addEdge(i, n, "", true);
            }
        }

        DeepWalk<String, String> deepWalk = new DeepWalk.Builder<String, String>()
                .vectorSize(vectorSize)
                .windowSize(windowSize)
                .learningRate(learningRate)
                .build();
        deepWalk.initialize(graph);

        for (int i = 0; i < vertexList.size(); i++) {
            INDArray vector = deepWalk.getVertexVector(i);
            result.add(Arrays.stream(vector.toDoubleVector()).boxed().collect(Collectors.toList()));
        }
        return result;
    }

}
