package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class VarAssignStmt extends Stmt {
    public final Identifier var;
    public final Expr value;

    public VarAssignStmt(Location left, Location right, Identifier var, Expr value) {
        super(left, right);
        this.var = var;
        this.value = value;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
