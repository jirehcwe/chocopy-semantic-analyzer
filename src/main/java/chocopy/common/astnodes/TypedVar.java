package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class TypedVar extends Node {

    public final Identifier identifier;
    public final TypeAnnotation type;

    public TypedVar(Location left, Location right, Identifier identifier, TypeAnnotation type) {
        super(left, right);
        this.identifier = identifier;
        this.type = type;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
