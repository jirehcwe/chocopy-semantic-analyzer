package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class MemberAssignExpr extends Expr {
    public final MemberExpr objectMember;
    public final Expr value;

    public MemberAssignExpr(Location left, Location right, MemberExpr objectMember, Expr value) {
        super(left, right);
        this.objectMember = objectMember;
        this.value = value;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
