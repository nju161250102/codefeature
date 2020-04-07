import ast.ASTFeature;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.CommentTools;
import tools.SymbolTools;
import word2vec.Word2VecModel;

import java.io.*;
import java.util.List;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String fileSeparator = File.separator;
        String parentPath = args[0];
        String targetPath = args[1];
        try {
            for (String type : new String[]{"Positive", "False"}) {
                BufferedWriter bw1 = new BufferedWriter(new FileWriter(targetPath + fileSeparator + type + ".csv"));
                for (File f1 : new File(parentPath + fileSeparator + type).listFiles()) {
                    for (File f2 : f1.listFiles()) {
                        File javaFile = f2.listFiles()[0];
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
                                bw1.write(name + "," + f2.getName());
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
                            e1.printStackTrace();
                        } catch (ParseProblemException e2) {
                            logger.error("Parser Error: " + javaFile.getName());
                        }
                    }
                }
                bw1.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
        }

    }

}
