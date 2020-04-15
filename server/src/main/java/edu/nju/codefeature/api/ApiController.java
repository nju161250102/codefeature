package edu.nju.codefeature.api;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import edu.nju.codefeature.ast.ASTFeature;
import edu.nju.codefeature.tools.CommentTools;
import edu.nju.codefeature.tools.SymbolTools;
import edu.nju.codefeature.word2vec.Word2VecModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final String pythonPath = "/usr/bin/python3.6";
    private final String trainPath = "./Train.py";
    private final String predictPath = "./Predict.py";

    @PostMapping("/extract")
    public List<ExtractResult> extract(@RequestBody ExtractRequest request) {
        String fileSeparator = File.separator;
        String parentPath = request.getDataPath();
        String targetPath = request.getOutputPath();
        List<ExtractResult> result = new ArrayList<>();

        try {
            for (String type : new String[]{"Positive", "False"}) {
                BufferedWriter bw1 = new BufferedWriter(new FileWriter(targetPath + fileSeparator + type + ".csv"));
                for (String path: getFilePath(parentPath + fileSeparator + type)) {
                    ExtractResult extractResult = new ExtractResult();

                    File javaFile = new File(path);
                    String name = javaFile.getName().split("\\.")[0];
                    new File(targetPath + fileSeparator + type).mkdir();
                    BufferedWriter bw2 = new BufferedWriter(new FileWriter( targetPath + fileSeparator + type + fileSeparator + name + ".csv"));
                    try {
                        logger.info("Start parse: " + javaFile.getName());
                        CompilationUnit cu = StaticJavaParser.parse(javaFile);
                        List<MethodDeclaration> methodList = cu.findAll(MethodDeclaration.class);
                        for (MethodDeclaration m : methodList) {
                            if (m.isConstructorDeclaration()) continue;
                            CommentTools.removeComment(m);
                            SymbolTools.nameSubstitute(m);
                            List<String> astWords = ASTFeature.extract(m);
                            extractResult.setSequence(astWords.size());
                            bw1.write(name);
                            for (String s: astWords) {
                                bw1.write("," + s);
                            }
                            bw1.newLine();
                            for (List<Double> line: Word2VecModel.transformWords(astWords)) {
                                for (int i = 0; i < line.size(); i++) {
                                    if (i == 0) bw2.write(line.get(0).toString());
                                    else bw2.write("," + line.get(i));
                                }
                                bw2.newLine();
                            }
                        }
                        bw2.close();
                    } catch (FileNotFoundException e1) {
                        logger.error("File Not Found: " + javaFile.getPath());
                        extractResult.setSuccess(false);
                        e1.printStackTrace();
                    } catch (ParseProblemException e2) {
                        logger.error("Parser Error: " + javaFile.getName());
                        extractResult.setSuccess(false);
                    }
                    extractResult.setName(name);
                    extractResult.setFlag(type);
                    result.add(extractResult);
                }
                bw1.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        return result;
    }

    @PostMapping("/train")
    public String train(@RequestBody TrainRequest request) {
        StringBuilder builder = new StringBuilder("[");
        String[] params = {pythonPath, trainPath, request.getOutputPath(), request.getModelPath()
                , String.valueOf(request.getEpochNum()), String.valueOf(request.getFeatureSize())};

        try {
            Process proc = Runtime.getRuntime().exec(String.join(" ", params));
            //用输入输出流来截取结果
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = in.readLine();
            while (true) {
                System.out.println(line);
                builder.append(line);
                if ((line = in.readLine()) != null) {
                    builder.append(",");
                } else {
                    break;
                }
            }
            in.close();
            proc.waitFor();
        }  catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        builder.append("]");

        return builder.toString().replace("'", "\"");
    }

    @GetMapping("/modelNum")
    public String getModelNumber() {
        String[] params = {pythonPath, trainPath};
        String line = null;
        try {
            Process proc = Runtime.getRuntime().exec(String.join(" ", params));
            //用输入输出流来截取结果
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            line = in.readLine();

            in.close();
            proc.waitFor();
        }  catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return "{\"modelNum\": " + line + "}";
    }

    @PostMapping("/predict")
    public String predict(@RequestBody PredictRequest request) {
        String fileSeparator = File.separator;
        String parentPath = request.getModelPath();

        try {
            BufferedWriter astWriter = new BufferedWriter(new FileWriter( parentPath + fileSeparator + "AST.csv"));
            try {
                logger.info("Start parse: " + request.getJavaFilePath());
                CompilationUnit cu = StaticJavaParser.parse(new File(request.getJavaFilePath()));
                List<MethodDeclaration> methodList = cu.findAll(MethodDeclaration.class);
                for (MethodDeclaration m : methodList) {
                    if (m.isConstructorDeclaration()) continue;
                    CommentTools.removeComment(m);
                    SymbolTools.nameSubstitute(m);
                    List<String> astWords = ASTFeature.extract(m);
                    for (List<Double> line: Word2VecModel.transformWords(astWords)) {
                        for (int i = 0; i < line.size(); i++) {
                            if (i == 0) astWriter.write(line.get(0).toString());
                            else astWriter.write("," + line.get(i));
                        }
                        astWriter.newLine();
                    }
                }
                astWriter.close();
            } catch (FileNotFoundException e1) {
                logger.error("File Not Found: " + request.getJavaFilePath());
                e1.printStackTrace();
            } catch (ParseProblemException e2) {
                logger.error("Parser Error: " + request.getJavaFilePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] params = {pythonPath, predictPath, parentPath};
        String line = null;
        System.out.println(String.join(" ", params));
        try {
            Process proc = Runtime.getRuntime().exec(String.join(" ", params));
            //用输入输出流来截取结果
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            line = in.readLine();
            System.out.println(line);
            line = line.replace("'", "\"");
            in.close();
            proc.waitFor();
        }  catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        new File(parentPath + fileSeparator + "AST.csv").delete();
        return line;
    }

    private List<String> getFilePath(String path) {
        List<String> result = new ArrayList<>();

        File parentPath = new File(path);
        if ((! parentPath.exists()) || parentPath.isFile()) return result;

        for (File f1: parentPath.listFiles()) {
            if ((! f1.exists()) || f1.isFile()) continue;
            for (File f2 : f1.listFiles()) {
                if ((! f2.exists()) || f2.isFile()) continue;
                File javaFile = f2.listFiles()[0];
                result.add(javaFile.getAbsolutePath());
            }
        }
        return result;
    }
}
