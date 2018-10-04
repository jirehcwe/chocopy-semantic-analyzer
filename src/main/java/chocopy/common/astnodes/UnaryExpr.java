package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class UnaryExpr extends Expr {

    public final String operator;
    public final Expr operand;

    public UnaryExpr(Location left, Location right, String operator, Expr operand) {
        super(left, right);
        this.operator = operator;
        this.operand = operand;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
