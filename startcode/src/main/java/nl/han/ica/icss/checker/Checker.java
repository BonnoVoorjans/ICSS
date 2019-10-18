package nl.han.ica.icss.checker;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.types.*;

public class Checker {

    private LinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new LinkedList<>();
        variableTypes.add(new HashMap<>());

        for (ASTNode node : ast.root.getChildren()) {
            CH01OnlyDefinedVariablesGetUsed(node);
            CH02OperandsAreEqual(node);
            CH03NoColorLiteralInOperation(node);
        }
    }

    public void CH01OnlyDefinedVariablesGetUsed(ASTNode node) {
        if (node instanceof VariableAssignment) {
            variableTypes.get(0).put(((VariableAssignment) node).name.name, getExpressionType((Expression) node));
        }
        if (node instanceof VariableReference) {
            if (variableTypes.get(0).get(((VariableReference) node).name) == null) {
                node.setError("Deze variabele is nog niet geïnstantieerd");
            }
        }

        for(ASTNode nodes : node.getChildren()){
            CH01OnlyDefinedVariablesGetUsed(nodes);
        }
    }

    public void CH02OperandsAreEqual(ASTNode node) {

    }

    public void CH03NoColorLiteralInOperation(ASTNode node) {

        if (node instanceof Operation) {
            if (((Operation) node).lhs instanceof ColorLiteral || ((Operation) node).rhs instanceof ColorLiteral) {
                node.setError("A colorliteral is being used in an operation");
            }
        }
        for (ASTNode nodes : node.getChildren()) {
            CH03NoColorLiteralInOperation(nodes);
        }
    }

    private ExpressionType getExpressionType(Expression expression) {
        if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        }
        return null;
    }
}
/*
  PIXEL,
    PERCENTAGE,
    COLOR,
    SCALAR,
    UNDEFINED,
    BOOL
 */