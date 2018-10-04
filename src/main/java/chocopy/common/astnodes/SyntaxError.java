package chocopy.common.astnodes;

import java.util.Arrays;
import java.util.Objects;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;


public class SyntaxError extends CompilerError {
    public final String message;

    public SyntaxError(Location left, Location right, String message) {
        super(left, right);
        this.message = message;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemanticError that = (SemanticError) o;
        return Objects.equals(message, that.message) &&
                Arrays.equals(getLocation(), that.getLocation());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(message);
        result = 31 * result + Arrays.hashCode(getLocation());
        return result;
    }
}
