package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

public abstract class CompilerError extends Node {
    public CompilerError(Location left, Location right) {
        super(left, right);
    }
}
