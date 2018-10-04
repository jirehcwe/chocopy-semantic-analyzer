package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class ReturnStmt extends Stmt {

    public final Expr value;

    public ReturnStmt(Location left, Location right, Expr value) {
        super(left, right);
        this.value = value;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
