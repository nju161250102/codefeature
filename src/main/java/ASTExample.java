import ast.ASTFeature;
import cfg.CFGFeature;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.DotPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.CommentTools;
import tools.SymbolTools;
import word2vec.Word2VecModel;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class ASTExample {

    private static Logger logger = LoggerFactory.getLogger("ASTMain");

    public static void main(String[] args) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(new File("./src/main/java/example/CWE15_External_Control_of_System_or_Configuration_Setting__Environment_75b.java"));
            List<MethodDeclaration> methodList = cu.findAll(MethodDeclaration.class);
            System.out.println(new DotPrinter(false).output(cu));
            for (MethodDeclaration m : methodList) {
                String s = m.toString();
                CommentTools.removeComment(m);
                System.out.println(CFGFeature.extract(m).toDot());
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
        }
    }

}
