package chocopy.common.astnodes;

import java.util.List;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class IfStmt extends Stmt {
    public final Expr condition;
    public final List<Stmt> thenBody;
    public final List<Stmt> elseBody;

    public IfStmt(Location left, Location right, Expr condition, List<Stmt> thenBody, List<Stmt> elseBody) {
        super(left, right);
        this.condition = condition;
        this.thenBody = thenBody;
        this.elseBody = elseBody;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
