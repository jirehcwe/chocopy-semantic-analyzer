package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class IndexAssignStmt extends Stmt {
    public final IndexExpr listElement;
    public final Expr value;

    public IndexAssignStmt(Location left, Location right, IndexExpr listElement, Expr value) {
        super(left, right);
        this.listElement = listElement;
        this.value = value;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
