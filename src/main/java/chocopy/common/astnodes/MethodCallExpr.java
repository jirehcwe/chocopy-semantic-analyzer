package chocopy.common.astnodes;

import java.util.List;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class MethodCallExpr extends Expr {

    public final MemberExpr method;
    public final List<Expr> args;

    public MethodCallExpr(Location left, Location right, MemberExpr method, List<Expr> args) {
        super(left, right);
        this.method = method;
        this.args = args;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
