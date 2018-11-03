# CS 164: Programming Assignment 2

## Getting started

Run the following command to build your semantic analysis, and then run all the provided tests:

```
mvn clean package

java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy --pa2 chocopy.pa2.StudentAnalysis --dir src/test/data/pa2/sample/ --test
```

In the starter code, only two tests should pass. Your objective is to implement a semantic analysis that passes all the provided tests and meets the assignment specifications.

You can also run the semantic analysis on one input file at at time. In general, running the semantic analysis on a ChocoPy program is a two-step process. First, run the reference parser to get an AST JSON:

```
java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy --pa1 chocopy.reference.RefParser --in <chocopy_input_file> --out <ast_json_file>
```

Second, run the semantic analysis on the AST JSON to get a typed AST JSON:
```
java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy --pa2 chocopy.pa2.StudentAnalysis --in <ast_json_file> --out <typed_ast_json_file>
```

The `src/tests/data/pa2/sample` directory already contains the AST JSONs for the test programs (with extension `.out`); therefore, you can skip the first step for the sample test programs.

To observe the output of the reference implementation of the semantic analysis, replace the second step with the following command:

```
java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy --pa2 chocopy.reference.RefAnalysis --in <ast_json_file> --out <typed_ast_json_file>
```

In either step, you can omit the `--out <output_file>` argument to have the JSON be printed to standard output instead.

## Assignment specifications

See `PA2.pdf` for a detailed specification of the assignment.

Refer to `chocopy_language_reference.pdf` for specifications on the ChocoPy language. 

## Receiving updates to this repository

Add the `upstream` repository remotes (you only need to do this once in your local clone):

```
git remote add upstream https://github.com/cs164fall2018/pa2-chocopy-semantic-analysis.git
```

To sync with updates upstream:
```
git pull upstream master
```

## Submission writeup

Team member 1: Dean Li

Team member 2: Jireh Chew

1) Our semantic analysis does 2 passes on the AST, 1 DeclarationAnalyzer to store all declarations in a symbol table
and the other pass TypeChecker to ensure the program is properly checked.

2) Implementing the global and nonlocal scoping of ChocoPy. It was difficult to process outer scope and pass in proper
parameters to the GlobalDecl and NonlocalDecl analyzers to map the variables to the proper scope, so we had to create 
better SymbolTypes to hold information and references to the parent SymbolTables.

3) For example, we cannot allow "Hi" - 3 to take on the inferred type object, as we cannot resolve the arithmetic operation
between them, and should not assume the type of a which the expression is assigned to as an object.

4) We ensure that our primitive types will be distinguishable from created objects. Furthermore, when the compiler tries to 
optimise in subsequent steps, it can ensure that the value is applicable and does not need to bother if we are doing 
operations using a null value as it is guaranteed to be not null. For example, we need to handle the case of a null value of an int multipled by e.g 5 (5 * null). Thus, we know the result is also an int if it is guaranteed to be non-null.
