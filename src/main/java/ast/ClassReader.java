package ast;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassReader {

    public static Map<String, List<String>> methodTooWords(String filePath) {
        Map<String, List<String>> result = new HashMap<>();
        try {
            CompilationUnit cu = StaticJavaParser.parse(new File(filePath));
            List<MethodDeclaration> methodList = cu.findAll(MethodDeclaration.class);
            for (MethodDeclaration m : methodList) {
                List<String> words = new ArrayList<>();
                VoidVisitor<List<String>> voidVisitor = new MethodVisitor();
                voidVisitor.visit(m, words);
                System.out.println(words);
                result.put(m.getNameAsString(), words);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
        }
        return result;
    }
}
