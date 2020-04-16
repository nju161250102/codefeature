package edu.nju.codefeature.tools;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import edu.nju.codefeature.api.ExtractResult;
import edu.nju.codefeature.ast.ASTFeature;
import edu.nju.codefeature.cfg.BasicBlock;
import edu.nju.codefeature.cfg.BasicBlockGraph;
import edu.nju.codefeature.cfg.CFGFeature;
import edu.nju.codefeature.deepwalk.GraphFeature;
import edu.nju.codefeature.word2vec.Word2VecModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class FileTools {

    private static final Logger logger = LoggerFactory.getLogger(FileTools.class);
    public static int vectorSize = 16;

    public static void checkOutputDir(String outputDir) {
        File target = new File(outputDir);
        if (! target.exists()) {
            target.mkdir();
        }
        for (String type: new String[]{"Text", "WordVector", "Edge", "DeepWalk", "ParagraphVec"}) {
            target = new File(outputDir + File.separator + type);
            if (! target.exists()) {
                target.mkdir();
            }
        }
    }

    public static ExtractResult saveFeature(File javaFile, String outputDir) {
        ExtractResult extractResult = new ExtractResult();
        String javaFileName = javaFile.getName();
        extractResult.setName(javaFileName);
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);
            List<MethodDeclaration> methodList = cu.findAll(MethodDeclaration.class);
            for (MethodDeclaration m : methodList) {
                if (m.isConstructorDeclaration()) continue;
                CommentTools.removeComment(m);
                SymbolTools.nameSubstitute(m);
                try {
                    String outputName = javaFileName + "_" + m.getNameAsString() + ".csv";
                    List<String> astWords = ASTFeature.extract(m);
                    extractResult.setSequence(astWords.size());
                    FileTools.saveASTWords(astWords, outputDir, outputName);
                    FileTools.saveWordVector(astWords, outputDir, outputName);
                    BasicBlockGraph graph = CFGFeature.extractBasicBlocks(m);
                    extractResult.setBasicBlock(graph.getBasicBlocks().size());
                    FileTools.saveGraph(graph, outputDir, outputName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e1) {
            logger.error("File Not Found: " + javaFile.getAbsolutePath());
            e1.printStackTrace();
            extractResult.setSuccess(false);
        } catch (ParseProblemException e2) {
            logger.error("Parser Error: " + javaFile.getAbsolutePath());
            e2.printStackTrace();
            extractResult.setSuccess(false);
        }
        return extractResult;
    }

    private static void saveASTWords(List<String> astWords, String outputDir, String outputName) throws IOException {
        String outputPath = String.join(File.separator, new String[]{outputDir, "Text", outputName});
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
        FileTools.saveList(writer, astWords);
        writer.close();
    }

    private static void saveWordVector(List<String> words, String outputDir, String outputName) throws IOException {
        String outputPath = String.join(File.separator, new String[]{outputDir, "WordVector", outputName});
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
        for (List<Double> line: Word2VecModel.transformWords(words, vectorSize)) {
            FileTools.saveList(writer, line);
        }
        writer.close();
    }

    private static void saveGraph(BasicBlockGraph graph, String outputDir, String outputName) throws IOException {
        String edgePath = String.join(File.separator, new String[]{outputDir, "Edge", outputName});
        String deepWalkPath = String.join(File.separator, new String[]{outputDir, "DeepWalk", outputName});
        String paragraphVecPath = String.join(File.separator, new String[]{outputDir, "ParagraphVec", outputName});

        BufferedWriter writer = new BufferedWriter(new FileWriter(edgePath));
        for (List<Integer> line: graph.getEdges()) {
            FileTools.saveList(writer, line);
        }
        writer.close();

        List<List<Double>> deepWalkResult = GraphFeature.extract(graph, vectorSize);
        if (deepWalkResult.size() > 0) {
            writer = new BufferedWriter(new FileWriter(deepWalkPath));
            for (List<Double> line: deepWalkResult) {
                FileTools.saveList(writer, line);
            }
            writer.close();
        }

        writer = new BufferedWriter(new FileWriter(paragraphVecPath));
        List<String> paragraph = graph.getBasicBlocks()
                .stream()
                .map(BasicBlock::toString)
                .collect(Collectors.toList());
        for (List<Double> line: Word2VecModel.transformParagraph(paragraph, vectorSize)) {
            FileTools.saveList(writer, line);
        }
        writer.close();
    }

    private static void saveList(BufferedWriter bw, List list) throws IOException {
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) bw.write(list.get(0).toString());
            else bw.write("," + list.get(i).toString());
        }
        bw.newLine();
    }
}
