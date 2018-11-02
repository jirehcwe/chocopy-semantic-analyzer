package chocopy.pa2;

import java.io.IOException;
import java.util.ArrayList;

import chocopy.common.analysis.SymbolTable;
import chocopy.common.analysis.types.ClassDefType;
import chocopy.common.analysis.types.FunctionDefType;
import chocopy.common.analysis.types.SymbolType;
import chocopy.common.analysis.types.ValueType;
import chocopy.common.astnodes.Errors;
import chocopy.common.astnodes.Identifier;
import chocopy.common.astnodes.Node;
import chocopy.common.astnodes.Program;
import com.fasterxml.jackson.core.JsonProcessingException;

public class StudentAnalysis {

    public static SymbolTable<SymbolType> globalScope;

    public static String process(String input, boolean debug) throws IOException {
        try {
            Node ast = Node.fromJSON(input);

            // Only perform semantic analysis if the input
            // is a valid AST (i.e., not a syntax error)
            if (ast instanceof Program) {
                Program program = (Program) ast;
                // Collect errors into this data structure
                Errors errors = new Errors(new ArrayList<>());
                // Pass: build symbol tables for global, function, and class scopes
                DeclarationAnalyzer declarationAnalyzer = new DeclarationAnalyzer(errors);
                program.dispatch(declarationAnalyzer);
                globalScope = declarationAnalyzer.getGlobals();

                if (errors.hasErrors()) {
                    // If errors, then return error node
                    ast = errors;
                } else  {
                    // Pass: type check expressions, assignments, and function returns
                    TypeChecker typeChecker = new TypeChecker(globalScope);
                    program.dispatch(typeChecker);
                }

            }

            return ast.toJSON();
        } catch (JsonProcessingException e) {
            System.err.println("Input to semantic analysis phase is not a valid AST JSON");
            if (debug) {
                e.printStackTrace();
            }
            return "{}";
        }
    }

}
