package chocopy.common.analysis.types;

import chocopy.common.analysis.SymbolTable;
import chocopy.common.astnodes.Identifier;

public class ClassDefType extends SymbolType{
    public final String superclass;
    public final String className;
    public SymbolTable<SymbolType> currentScope;

    public ClassDefType(String parent, String self){
        superclass = parent;
        className = self;
        currentScope = new SymbolTable<>();
    }
}
