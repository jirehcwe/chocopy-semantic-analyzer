package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** A statement containing only an expression. */
public final class ExprStmt extends Stmt {

    public final Expr expr;

    public ExprStmt(Location left, Location right, Expr expr) {
        super(left, right);
        this.expr = expr;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
