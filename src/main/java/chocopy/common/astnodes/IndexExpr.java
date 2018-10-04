package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class IndexExpr extends Expr {

    public final Expr list;
    public final Expr index;

    public IndexExpr(Location left, Location right, Expr list, Expr index) {
        super(left, right);
        this.list = list;
        this.index = index;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
