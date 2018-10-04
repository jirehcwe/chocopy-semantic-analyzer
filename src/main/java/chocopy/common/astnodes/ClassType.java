package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public final class ClassType extends TypeAnnotation {

    public final String className;

    public ClassType(Location left, Location right, String className) {
        super(left, right);
        this.className = className;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}