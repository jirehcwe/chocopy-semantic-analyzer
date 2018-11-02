package chocopy.common.analysis.types;

import chocopy.common.analysis.SymbolTable;
import chocopy.common.astnodes.TypeAnnotation;
import chocopy.common.astnodes.TypedVar;
import java.util.List;

public class FunctionDefType extends SymbolType {
    public String funcName;
    public SymbolTable<SymbolType> currentScope = new SymbolTable<>();
    public List<TypedVar> params;
    public TypeAnnotation returnType;
    public String outerFuncName;


}
