package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class VarDef extends Declaration {
    public final TypedVar var;
    public final Literal value;

    public VarDef(Location left, Location right, TypedVar var, Literal value) {
        super(left, right);
        this.var = var;
        this.value = value;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

    @Override
    public Identifier getIdentifier() {
        return this.var.identifier;
    }
}
