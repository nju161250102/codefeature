package edu.nju.codefeature.api;

import edu.nju.codefeature.tools.FileTools;
import edu.nju.codefeature.tools.PythonTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Value("${pythonConfig.path}")
    private String pythonPath;
    @Value("${pythonConfig.train}")
    private String trainPath;
    @Value("${pythonConfig.predict}")
    private String predictPath;

    @PostMapping("/extract")
    public List<ExtractResult> extract(@RequestBody ExtractRequest request) {
        String fileSeparator = File.separator;
        String parentPath = request.getDataPath();
        String targetPath = request.getOutputPath();
        int vectorSize = request.getFeatureSize();
        List<ExtractResult> result = new ArrayList<>();

        for (String type : new String[]{"Positive", "False"}) {
            String outputDir = targetPath + fileSeparator + type;
            FileTools.checkOutputDir(outputDir);
            FileTools.vectorSize = vectorSize;
            for (String path: getFilePath(parentPath + fileSeparator + type)) {
                File javaFile = new File(path);
                ExtractResult extractResult = FileTools.saveFeature(javaFile, outputDir);
                extractResult.setFlag("Positive".equals(type) ? "正报" : "误报");
                result.add(extractResult);
            }
        }
        return result;
    }

    @PostMapping("/train")
    public String train(@RequestBody TrainRequest request) {
        String[] params = {pythonPath, trainPath, request.getOutputPath(), request.getModelPath()
                , String.valueOf(request.getEpochNum()), String.valueOf(request.getFeatureSize())};

        return PythonTools.execute(params);
    }

    @GetMapping("/modelNum")
    public String getModelNumber() {
        String[] params = {pythonPath, trainPath};
        return PythonTools.execute(params);
    }

    @PostMapping("/predict")
    public String predict(@RequestBody PredictRequest request) {
        String fileSeparator = File.separator;
        String parentPath = request.getModelPath();

        FileTools.saveFeature(new File(request.getJavaFilePath()), parentPath);

        String[] params = {pythonPath, predictPath, parentPath};
        String line = PythonTools.execute(params);

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
