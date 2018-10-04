package chocopy.common.astnodes;

import java.util.List;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class WhileStmt extends Stmt {
    public final Expr condition;
    public final List<Stmt> body;

    public WhileStmt(Location left, Location right, Expr condition, List<Stmt> body) {
        super(left, right);
        this.condition = condition;
        this.body = body;
    }


    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
