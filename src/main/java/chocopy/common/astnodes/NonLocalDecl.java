package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class NonLocalDecl extends Declaration {

    public final Identifier variable;

    public NonLocalDecl(Location left, Location right, Identifier variable) {
        super(left, right);
        this.variable = variable;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

    @Override
    public Identifier getIdentifier() {
        return this.variable;
    }
}
