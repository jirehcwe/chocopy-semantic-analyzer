package chocopy.common.astnodes;

import java.util.List;

import chocopy.common.analysis.NodeAnalyzer;
import com.fasterxml.jackson.annotation.JsonCreator;

public class Errors extends Node {

    public final List<CompilerError> errors;

    @JsonCreator // This is needed because Jackson gets confused with hasErrors()
    public Errors(List<CompilerError> errors) {
        super(null, null);
        this.errors = errors;
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public void add(CompilerError err) {
        errors.add(err);
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
