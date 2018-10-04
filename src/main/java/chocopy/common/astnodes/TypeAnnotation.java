package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Base of all AST nodes representing type annotations.
 */
public abstract class TypeAnnotation extends Node {
    public TypeAnnotation(Location left, Location right) {
        super(left, right);
    }
}
