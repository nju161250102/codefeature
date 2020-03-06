package cfg;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class MethodCFGVisitor extends VoidVisitorAdapter<CFG> {

    @Override
    public void visit(MethodDeclaration method, CFG cfg) {
        if (method.getBody().isPresent()) {
            BlockStmt blockStmt = method.getBody().get();
            cfg.addNode(blockStmt);
            cfg.setSourceNodes(blockStmt);
            cfg.setSinkNodes(blockStmt);
            super.visit(method, cfg);
        }
    }

    @Override
    public void visit(BlockStmt stmt, CFG cfg) {
        CFG thisCFG = new CFG();
        NodeList<Statement> nodeList = stmt.getStatements();
        for (int i = 0; i < nodeList.size(); i++) {
            thisCFG.addNode(nodeList.get(i));
            if (i > 0) {
                thisCFG.addEdge(nodeList.get(i-1), nodeList.get(i));
            }
        }
        thisCFG.setSourceNodes(nodeList.get(0));
        thisCFG.setSinkNodes(nodeList.get(nodeList.size() - 1));
        cfg.mergeCFG(stmt, thisCFG);
        super.visit(stmt, cfg);
    }

    @Override
    public void visit(IfStmt stmt, CFG cfg) {
        CFG thisCFG = new CFG();
        thisCFG.setSourceNodes(stmt.getCondition());
        thisCFG.addEdge(stmt.getCondition(), stmt.getThenStmt());
        if (stmt.getElseStmt().isPresent()) {
            thisCFG.addEdge(stmt.getCondition(), stmt.getElseStmt().get());
            thisCFG.setSinkNodes(stmt.getThenStmt(), stmt.getElseStmt().get());
        } else {
            thisCFG.setSinkNodes(stmt.getCondition(), stmt.getThenStmt());
        }
        cfg.mergeCFG(stmt, thisCFG);
        super.visit(stmt, cfg);
    }

    @Override
    public void visit(WhileStmt stmt, CFG cfg) {
        CFG thisCFG = new CFG();
        thisCFG.setSourceNodes(stmt.getCondition());
        thisCFG.addEdge(stmt.getCondition(), stmt.getBody());
        thisCFG.addEdge(stmt.getBody(), stmt.getCondition());
        thisCFG.setSinkNodes(stmt.getCondition());
        cfg.mergeCFG(stmt, thisCFG);
        super.visit(stmt, cfg);
    }

    @Override
    public void visit(DoStmt stmt, CFG cfg) {
        CFG thisCFG = new CFG();
        thisCFG.setSourceNodes(stmt.getBody());
        thisCFG.addEdge(stmt.getBody(), stmt.getCondition());
        thisCFG.addEdge(stmt.getCondition(), stmt.getBody());
        thisCFG.setSinkNodes(stmt.getCondition());
        cfg.mergeCFG(stmt, thisCFG);
        super.visit(stmt, cfg);
    }

    @Override
    public void visit(ForStmt stmt, CFG cfg) {
        CFG thisCFG = new CFG();
        BlockStmt initStmt = this.ExpressionsToBlcok(stmt.getInitialization());
        BlockStmt updateSmt = this.ExpressionsToBlcok(stmt.getUpdate());
        thisCFG.setSourceNodes(initStmt);

        if (stmt.getCompare().isPresent()) {
            thisCFG.addEdge(initStmt, stmt.getCompare().get());
            thisCFG.addEdge(stmt.getCompare().get(), stmt.getBody());
            thisCFG.addEdge(stmt.getBody(), updateSmt);
            thisCFG.addEdge(updateSmt, stmt.getCompare().get());
            thisCFG.setSinkNodes(stmt.getCompare().get());
        } else {
            thisCFG.addEdge(initStmt, stmt.getBody());
            thisCFG.addEdge(stmt.getBody(), stmt.getBody());
            thisCFG.setSinkNodes(stmt.getBody());
        }
        super.visit(stmt, thisCFG);
        this.visit(initStmt, thisCFG);
        this.visit(updateSmt, thisCFG);
        for (Node node: thisCFG.getSinkNodes()) {
            CFGNode cfgNode = thisCFG.findNode(node);
            if (node instanceof BreakStmt) cfgNode.clearNextNode();
        }
        cfg.mergeCFG(stmt, thisCFG);
    }

    @Override
    public void visit(BreakStmt stmt, CFG cfg) {
        List<Node> sinkNodes = cfg.getSinkNodes();
        sinkNodes.add(stmt);
        cfg.setSinkNodes(sinkNodes);
        super.visit(stmt, cfg);
    }

    private BlockStmt ExpressionsToBlcok(NodeList<Expression> expressions) {
        NodeList<Statement> expressionStmts
                = expressions.stream().map(ExpressionStmt::new).collect(Collectors.toCollection(NodeList::new));
        return new BlockStmt(expressionStmts);
    }

}
