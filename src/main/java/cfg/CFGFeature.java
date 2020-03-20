package cfg;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.CommentTools;
import tools.SymbolTools;

public class CFGFeature {

    private static Logger logger = LoggerFactory.getLogger("CFGFeature");

    public static CFG extract(MethodDeclaration method) {
        // 清除注释
        CommentTools.removeComment(method);
        // 对变量重新命名
        SymbolTools.nameSubstitute(method);
        CFG cfg = new CFG();
        VoidVisitor<CFG> visitor = new MethodCFGVisitor();
        visitor.visit(method, cfg);
        logger.info("Extract CFG Feature From " + method.getNameAsString());
        return cfg;
    }

    public static BasicBlockGraph extractBasicBlocks(MethodDeclaration method) {
        CFG cfg = extract(method);
        logger.info("Extract Basic Graph From " + method.getNameAsString());
        return new BasicBlockGraph(cfg);
    }

}
