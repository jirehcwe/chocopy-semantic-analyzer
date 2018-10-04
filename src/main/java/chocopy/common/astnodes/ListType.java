package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public final class ListType extends TypeAnnotation {

    public final TypeAnnotation elementType;

    public ListType(Location left, Location right, TypeAnnotation elementType) {
        super(left, right);
        this.elementType = elementType;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}