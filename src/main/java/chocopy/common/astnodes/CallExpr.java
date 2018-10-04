package chocopy.common.astnodes;

import java.util.List;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class CallExpr extends Expr {

    public final Identifier function;
    public final List<Expr> args;

    public CallExpr(Location left, Location right, Identifier function, List<Expr> args) {
        super(left, right);
        this.function = function;
        this.args = args;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
