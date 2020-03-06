import ast.ClassReader;

import java.util.List;
import java.util.Map;

public class ASTExample {

    public static void main(String[] args) {
        Map<String, List<String>> astWords = ClassReader.methodTooWords("/home/qian/Desktop/2020/codefeature/src/main/java/example/CWE23_Relative_Path_Traversal__connect_tcp_01.java");
    }

}
