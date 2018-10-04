package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class MemberExpr extends Expr {

    public final Expr object;
    public final Identifier member;

    public MemberExpr(Location left, Location right, Expr object, Identifier member) {
        super(left, right);
        this.object = object;
        this.member = member;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
