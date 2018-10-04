package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class MemberAssignStmt extends Stmt {
    public final MemberExpr objectMember;
    public final Expr value;

    public MemberAssignStmt(Location left, Location right, MemberExpr objectMember, Expr value) {
        super(left, right);
        this.objectMember = objectMember;
        this.value = value;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
