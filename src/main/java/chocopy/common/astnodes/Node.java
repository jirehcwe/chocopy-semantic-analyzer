package chocopy.common.astnodes;

import java.io.IOException;

import chocopy.common.analysis.NodeAnalyzer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Root of the AST class hierarchy.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property="kind")
@JsonSubTypes({
        // List of all concrete subclasses of Node
        @JsonSubTypes.Type(BinaryExpr.class),
        @JsonSubTypes.Type(BooleanLiteral.class),
        @JsonSubTypes.Type(CallExpr.class),
        @JsonSubTypes.Type(ClassDef.class),
        @JsonSubTypes.Type(ClassType.class),
        @JsonSubTypes.Type(Errors.class),
        @JsonSubTypes.Type(ExprStmt.class),
        @JsonSubTypes.Type(ForStmt.class),
        @JsonSubTypes.Type(FuncDef.class),
        @JsonSubTypes.Type(GlobalDecl.class),
        @JsonSubTypes.Type(Identifier.class),
        @JsonSubTypes.Type(IfStmt.class),
        @JsonSubTypes.Type(IndexAssignExpr.class),
        @JsonSubTypes.Type(IndexAssignStmt.class),
        @JsonSubTypes.Type(IndexExpr.class),
        @JsonSubTypes.Type(IntegerLiteral.class),
        @JsonSubTypes.Type(ListExpr.class),
        @JsonSubTypes.Type(ListType.class),
        @JsonSubTypes.Type(MemberAssignExpr.class),
        @JsonSubTypes.Type(MemberAssignStmt.class),
        @JsonSubTypes.Type(MemberExpr.class),
        @JsonSubTypes.Type(MethodCallExpr.class),
        @JsonSubTypes.Type(NoneLiteral.class),
        @JsonSubTypes.Type(NonLocalDecl.class),
        @JsonSubTypes.Type(PassStmt.class),
        @JsonSubTypes.Type(Program.class),
        @JsonSubTypes.Type(ReturnStmt.class),
        @JsonSubTypes.Type(StringLiteral.class),
        @JsonSubTypes.Type(SemanticError.class),
        @JsonSubTypes.Type(SyntaxError.class),
        @JsonSubTypes.Type(TypedVar.class),
        @JsonSubTypes.Type(UnaryExpr.class),
        @JsonSubTypes.Type(VarAssignExpr.class),
        @JsonSubTypes.Type(VarAssignStmt.class),
        @JsonSubTypes.Type(VarDef.class),
        @JsonSubTypes.Type(WhileStmt.class),
})
public abstract class Node {

    public final String kind;
    protected int leftLine;
    protected int leftCol;
    protected int rightLine;
    protected int rightCol;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String typeError;

    public Node(Location left, Location right) {
        if (left != null) {
            this.leftLine = left.getLine();
            this.leftCol = left.getColumn();
        }
        if (right != null) {
            this.rightLine = right.getLine();
            this.rightCol = right.getColumn();
        }
        this.kind = getClass().getSimpleName();
    }

    public int[] getLocation() {
        return new int[]{ leftLine, leftCol, rightLine, rightCol} ;
    }

    public void setLocation(int[] location) {
        this.leftLine = location[0];
        this.leftCol = location[1];
        this.rightLine = location[2];
        this.rightCol = location[3];
    }

    /* AST node analyzer */
    public abstract <T> T dispatch(NodeAnalyzer<T> analyzer);

    /* Prints out the AST in JSON format */
    @Override
    public String toString() {
        try {
            return toJSON();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String toJSON() throws JsonProcessingException {
        return mapper.writeValueAsString(this);
    }

    static ObjectMapper mapper = new ObjectMapper();

    static {
        // mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, JsonTypeInfo.As.EXISTING_PROPERTY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new ParameterNamesModule());
    }

    /* Reads in a Node as a JSON object */
    public static Node fromJSON(String json) throws JsonProcessingException, IOException {
        return mapper.readValue(json, Node.class);
    }

}
