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
    public void visit(SwitchStmt stmt, CFG cfg) {
        CFG thisCFG = new CFG();
    }

    @Override
    public void visit(WhileStmt stmt, CFG cfg) {
        CFG thisCFG = new CFG();
        thisCFG.setSourceNodes(stmt.getCondition());
        thisCFG.addEdge(stmt.getCondition(), stmt.getBody());
        thisCFG.addEdge(stmt.getBody(), stmt.getCondition());
        thisCFG.setSinkNodes(stmt.getCondition());
        super.visit(stmt, thisCFG);
        this.loopCFGHandle(stmt, thisCFG);
        cfg.mergeCFG(stmt, thisCFG);
    }

    @Override
    public void visit(DoStmt stmt, CFG cfg) {
        CFG thisCFG = new CFG();
        thisCFG.setSourceNodes(stmt.getBody());
        thisCFG.addEdge(stmt.getBody(), stmt.getCondition());
        thisCFG.addEdge(stmt.getCondition(), stmt.getBody());
        thisCFG.setSinkNodes(stmt.getCondition());
        super.visit(stmt, thisCFG);
        this.loopCFGHandle(stmt, thisCFG);
        cfg.mergeCFG(stmt, thisCFG);
    }

    @Override
    public void visit(ForStmt stmt, CFG cfg) {
        CFG thisCFG = new CFG();
        BlockStmt initStmt = this.ExpressionsToBlock(stmt.getInitialization());
        BlockStmt updateSmt = this.ExpressionsToBlock(stmt.getUpdate());
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
//        for (Node node: thisCFG.getSinkNodes()) {
//            if (node instanceof BreakStmt) thisCFG.findNode(node).clearNextNode();
//        }
        this.loopCFGHandle(stmt, thisCFG);
        cfg.mergeCFG(stmt, thisCFG);
    }

    @Override
    public void visit(BreakStmt stmt, CFG cfg) {
        List<Node> sinkNodes = cfg.getSinkNodes();
        if (! sinkNodes.contains(stmt)) sinkNodes.add(stmt);
        cfg.setSinkNodes(sinkNodes);
        super.visit(stmt, cfg);
    }

    @Override
    public void visit(ContinueStmt stmt, CFG cfg) {

    }

    @Override
    public void visit(TryStmt stmt, CFG cfg) {
        CFG thisCFG = new CFG();
        thisCFG.setSourceNodes(stmt.getTryBlock());
        thisCFG.addNode(stmt.getTryBlock());
        if (stmt.getFinallyBlock().isPresent()) {
            thisCFG.addEdge(stmt.getTryBlock(), stmt.getFinallyBlock().get());
            thisCFG.setSinkNodes(stmt.getFinallyBlock().get());
        } else {
            thisCFG.setSinkNodes(stmt.getTryBlock());
        }
        cfg.mergeCFG(stmt, thisCFG);
        super.visit(stmt, cfg);
    }

    private BlockStmt ExpressionsToBlock(NodeList<Expression> expressions) {
        NodeList<Statement> expressionStmts
                = expressions.stream().map(ExpressionStmt::new).collect(Collectors.toCollection(NodeList::new));
        return new BlockStmt(expressionStmts);
    }

    private void loopCFGHandle(Statement stmt, CFG cfg) {
        for (Node v: cfg.getSinkNodes()) {
            if (v instanceof BreakStmt) cfg.findNode(v).clearNextNode();
        }

        for (CFGNode v1: cfg.getVertexList()) {
            if (v1.getNode() instanceof ContinueStmt) {
                v1.clearNextNode();
                if (stmt instanceof WhileStmt) {
                    cfg.addEdge(v1.getNode(), ((WhileStmt) stmt).getCondition());
                } else if (stmt instanceof DoStmt) {
                    cfg.addEdge(v1.getNode(), ((DoStmt) stmt).getCondition());
                } else if (stmt instanceof ForStmt) {
                    ForStmt forStmt = (ForStmt) stmt;
                    if (forStmt.getCompare().isPresent()) {
                        cfg.addEdge(v1.getNode(), forStmt.getCompare().get());
                    } else {
                        //
                        cfg.addEdge(v1.getNode(), forStmt.getCompare().get());
                    }
                }
                cfg.getSinkNodes().remove(v1.getNode());
            }
        }
    }

}
