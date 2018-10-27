package chocopy.pa2;

import chocopy.common.analysis.AbstractNodeAnalyzer;
import chocopy.common.analysis.SymbolTable;
import chocopy.common.analysis.types.SymbolType;
import chocopy.common.analysis.types.ValueType;
import chocopy.common.astnodes.Declaration;
import chocopy.common.astnodes.Errors;
import chocopy.common.astnodes.Identifier;
import chocopy.common.astnodes.Program;
import chocopy.common.astnodes.SemanticError;
import chocopy.common.astnodes.VarDef;

/**
 * Analyzes declarations to create a top-level symbol table
 */
public class DeclarationAnalyzer extends AbstractNodeAnalyzer<SymbolType> {

    private SymbolTable<SymbolType> sym = new SymbolTable<>();  // Changes with scope
    private final SymbolTable<SymbolType> globals = sym;        // always global
    private final Errors errors;

    public DeclarationAnalyzer(Errors errors) {
        this.errors = errors;
    }


    public SymbolTable<SymbolType> getGlobals() {
        return globals;
    }

    public SymbolType analyze(Program program) {
        // Process all global declarations recursively
        for (Declaration decl : program.declarations) {
            Identifier id = decl.getIdentifier();
            String name = id.name;

            // Process declaration
            SymbolType type = decl.dispatch(this);

            // Skip if error
            if (type == null) {
                //System.out.println("IT IS NULL!\n");
                continue;
            }
            //System.out.println("IT IS NOT NULL!\n");

            // Add declaration to symbol table if it is new
            if (sym.declares(name)) {
                errors.add(new SemanticError(id, "Duplicate declaration of identifier in same scope: " + name));
            } else {
                sym.put(name, type);
            }
        }

        return null;
    }

    public SymbolType analyze(VarDef varDef) {
        //System.out.println("USING VARDEF ANALYZE\n");
        return ValueType.annotationToValueType(varDef.var.type);
    }


}
