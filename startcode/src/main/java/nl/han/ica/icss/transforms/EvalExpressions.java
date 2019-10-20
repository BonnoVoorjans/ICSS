package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.HashMap;
import java.util.LinkedList;

public class EvalExpressions implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;

    public EvalExpressions() {
        variableValues = new LinkedList<>();
        variableValues.add(new HashMap<String, Literal>());
    }

    @Override
    public void apply(AST ast) {
        traverseThroughTree(ast.root);
    }


    public void traverseThroughTree(ASTNode node) {
        for (ASTNode nodes : node.getChildren()) {
            if (nodes instanceof VariableAssignment) {
                if (((VariableAssignment) nodes).expression instanceof Operation) {
                    Literal resultOfOperation = calculateOperation((Operation) ((VariableAssignment) nodes).expression);
                    nodes.removeChild(((VariableAssignment) nodes).expression);
                    nodes.addChild(resultOfOperation);
                    addVariableValueToList(((VariableAssignment) nodes).name.name, resultOfOperation);
                } else {
                    addVariableValueToList(((VariableAssignment) nodes).name.name, (Literal) ((VariableAssignment) nodes).expression);
                }
            }
            if(nodes instanceof VariableReference){
                String key = ((VariableReference) nodes).name;
                 node.removeChild(nodes);
                 node.addChild(variableValues.get(0).get(key));
            }

            if (nodes instanceof Operation) {
                Literal resultOfOperation = calculateOperation((Operation) nodes);
                node.removeChild(nodes);
                node.addChild(resultOfOperation);
            } else {
                traverseThroughTree(nodes);
            }
        }
    }

    private Literal calculateOperation(Operation node) {
        replaceChildOperationByLiteral(node);
        replaceChildReferencesByLiteral(node);

        if(node instanceof AddOperation) {
            if (node.lhs instanceof PercentageLiteral) {
                int resultValue = ((PercentageLiteral) node.lhs).value + ((PercentageLiteral) node.rhs).value;
                PercentageLiteral result = new PercentageLiteral(resultValue);
                return result;
            }
            else if (node.lhs instanceof PixelLiteral) {
                int resultValue = ((PixelLiteral) node.lhs).value + ((PixelLiteral) node.rhs).value;
                PixelLiteral result = new PixelLiteral(resultValue);
                return result;
            } else if (node.lhs instanceof PixelLiteral) {
                int resultValue = ((PercentageLiteral) node.lhs).value + ((PercentageLiteral) node.rhs).value;
                PixelLiteral result = new PixelLiteral(resultValue);
                return result;
            }
            else{
                int resultValue = ((ScalarLiteral)node.lhs).value + ((ScalarLiteral)node.rhs).value;
                ScalarLiteral result = new ScalarLiteral(resultValue);
                return result;
            }
        }

        if(node instanceof SubtractOperation){
            if (node.lhs instanceof PercentageLiteral) {
                int resultValue = ((PercentageLiteral) node.lhs).value - ((PercentageLiteral) node.rhs).value;
                PercentageLiteral result = new PercentageLiteral(resultValue);
                return result;
            }
            else if (node.lhs instanceof PixelLiteral) {
                int resultValue = ((PixelLiteral) node.lhs).value - ((PixelLiteral) node.rhs).value;
                PixelLiteral result = new PixelLiteral(resultValue);
                return result;
            } else if (node.lhs instanceof PixelLiteral) {
                int resultValue = ((PercentageLiteral) node.lhs).value - ((PercentageLiteral) node.rhs).value;
                PixelLiteral result = new PixelLiteral(resultValue);
                return result;
            }
            else{
                int resultValue = ((ScalarLiteral)node.lhs).value - ((ScalarLiteral)node.rhs).value;
                ScalarLiteral result = new ScalarLiteral(resultValue);
                return result;
            }
        }

        if(node instanceof MultiplyOperation){
            int resultValue = ((ScalarLiteral)node.lhs).value * ((ScalarLiteral)node.rhs).value;
            ScalarLiteral result = new ScalarLiteral(resultValue);
            return result;
        }
        return null;
    }

    private void addVariableValueToList(String key, Literal value) {
        variableValues.get(0).put(key, value);
    }

    private void replaceChildOperationByLiteral(Operation node) {
        if (node.lhs instanceof Operation) {
            Literal lit = calculateOperation((Operation) node.lhs);
            node.removeLeftChild();
            node.addChild(lit);
        }
        if (node.rhs instanceof Operation) {
            Literal lit = calculateOperation((Operation) node.rhs);
            node.removeRightChild();
            node.addChild(lit);
        }
    }

    private void replaceChildReferencesByLiteral(Operation node) {
        if (node.lhs instanceof VariableReference) {
            Literal lit = variableValues.get(0).get(((VariableReference) node.lhs).name);
            node.removeLeftChild();
            node.addChild(lit);
        }

        if (node.rhs instanceof VariableReference) {
            Literal lit = variableValues.get(0).get(((VariableReference) node.rhs).name);
            node.removeRightChild();
            node.addChild(lit);
        }
    }


}
