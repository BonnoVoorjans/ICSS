package nl.han.ica.icss.checker;

import java.util.*;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.*;

public class Checker {

    private final int GLOBALSCOPE = 0;
    private LinkedList<HashMap<String, ExpressionType>> variableTypes;
    private HashSet<ExpressionType> allowedSizeExpressions;
    private HashSet<ExpressionType> allowedColorExpressions;
    private int currentScope = 0;

    public void check(AST ast) {
        currentScope = 0;
        variableTypes = new LinkedList<>();
        variableTypes.add(new HashMap<>());
        allowedSizeExpressions = (HashSet) createSetOfExpressionTypesForSize();
        allowedColorExpressions = (HashSet) createSetOfExpressionTypesForColors();

        for (ASTNode node : ast.root.getChildren()) {
            CH01OnlyDefinedVariablesGetUsed(node);
            CH02OperandsAreEqual(node);
            CH03NoColorLiteralInOperation(node);
            CH04DeclarationContainsRightLiteral(node);
            CH05CheckIfConditionIsBoolean(node);
        }
    }

    public void CH01OnlyDefinedVariablesGetUsed(ASTNode node) {
        if (node instanceof Stylerule) {
            currentScope++;
            variableTypes.add(new HashMap<String, ExpressionType>());
        }
        if (node instanceof VariableAssignment) {
            variableTypes.get(currentScope).put(((VariableAssignment) node).name.name, getExpressionType(((VariableAssignment) node).expression));
        }
        if (node instanceof VariableReference) {
            if (!variableTypes.get(currentScope).containsKey(((VariableReference) node).name) &&
                    !variableTypes.get(GLOBALSCOPE).containsKey(((VariableReference) node).name)) {
                node.setError("Er worden een of meerdere variabelen gebruikt die niet in de scope zijn gedefinieërd");
            }
        }

        for (ASTNode nodes : node.getChildren()) {
            CH01OnlyDefinedVariablesGetUsed(nodes);
        }
    }

    public void CH02OperandsAreEqual(ASTNode node) {
        Set<ExpressionType> literalsInOperation = new HashSet<ExpressionType>();
        if (node instanceof Operation) {
            traverseThroughOperationAndGetLiteralTypes((Operation) node, literalsInOperation);
        }

        if (node instanceof MultiplyOperation) {
            if (!checkIfOnlyScalar(literalsInOperation)) {
                node.setError("Er wordt vermenigvuldigd met een literal die niet Scalar is");
            }
        }

        if (node instanceof AddOperation || node instanceof SubtractOperation) {
            if (!checkIfOnlyOneLiteralType(literalsInOperation)) {
                node.setError("Er worden verschillende soorten literals gebruikt bij een AddOperation of een SubtractOperation");
            }
        }

        for (ASTNode nodes : node.getChildren()) {
            CH02OperandsAreEqual(nodes);
        }

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

    public void CH04DeclarationContainsRightLiteral(ASTNode node) {
        if (node instanceof Declaration) {
            String propertyName = ((Declaration) node).property.name;
            if (((Declaration) node).expression instanceof Operation) {
                Set<ExpressionType> literalsInOperation = new HashSet<ExpressionType>();
                Expression e = ((Declaration) node).expression;
                traverseThroughOperationAndGetLiteralTypes((Operation) e, literalsInOperation);

                if (literalsInOperation.size() != 1) {
                    node.setError("The declaration contains an ammount of expression types unequal to 1");
                } else {
                    if (!checkIfAllowedPropertyExpressionTypeCombination(literalsInOperation.iterator().next(), propertyName)) { //Als er 1 ExpressionType is gevonden
                        node.setError("There is an illegal combination of a property and an expression type");  //haal het eerste item uit de HashSet op, en kijk of het een legale combinatie is.
                    }
                }
            }

            if (((Declaration) node).expression instanceof Literal) {
                ExpressionType type = getExpressionType(((Declaration) node).expression);
                if (!checkIfAllowedPropertyExpressionTypeCombination(type, propertyName)) {
                    node.setError("There is an illegal combination of a property and an expression type");
                }
            }

            if (((Declaration) node).expression instanceof VariableReference) {
                VariableReference reference = (VariableReference) ((Declaration) node).expression;
                ExpressionType type = variableTypes.get(0).get(reference.name);
                if (!checkIfAllowedPropertyExpressionTypeCombination(type, propertyName)) {
                    node.setError("There is an illegal combination of a property and an expression type");
                }
            }
        }

        for (ASTNode nodes : node.getChildren()) {
            CH04DeclarationContainsRightLiteral(nodes);
        }
    }


    public void CH05CheckIfConditionIsBoolean(ASTNode node) {
        if (node instanceof IfClause) {
            Expression e = ((IfClause) node).conditionalExpression;

            if (e instanceof VariableReference) {
                for (HashMap<String, ExpressionType> map : variableTypes) {
                    if (map.containsKey(((VariableReference) e).name)) {
                        if (map.get(((VariableReference) e).name) != ExpressionType.BOOL) {
                            node.setError("Je gebruikt een varref naar een niet boolean var in je if statement");
                        }
                    }
                }
            } else if (!(e instanceof BoolLiteral)) {
                node.setError("Je gebruikt een niet-boolean condition in je if statement");
            }
        }


        for (ASTNode nodes : node.getChildren()) {
            CH05CheckIfConditionIsBoolean(nodes);
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

    private boolean checkIfOnlyOneLiteralType(Set<ExpressionType> literals) {
        if (literals.contains(null)) {
            literals.remove(null);
        }
        if (literals.size() > 1) {
            return false;
        }
        return true;
    }

    private boolean checkIfOnlyScalar(Set<ExpressionType> literals) {
        if (literals.contains(null)) {
            literals.remove(null);
        }

        for (ExpressionType type : literals) {
            if (type != ExpressionType.SCALAR) {
                return false;
            }
        }
        return true;
    }

    private void traverseThroughOperationAndGetLiteralTypes(Operation operation, Set<ExpressionType> list) {
        if (operation.lhs instanceof Literal) {
            list.add(getExpressionType((Expression) operation.lhs));
        } else if (operation.lhs instanceof VariableReference) {
            addExpressionTypeOfReferenceToList((VariableReference) operation.lhs, list);
        } else {
            traverseThroughOperationAndGetLiteralTypes((Operation) operation.lhs, list);
        }

        if (operation.rhs instanceof Literal) {
            list.add(getExpressionType((Expression) operation.rhs));
        } else if (operation.rhs instanceof VariableReference) {
            addExpressionTypeOfReferenceToList((VariableReference) operation.rhs, list);
        } else {
            traverseThroughOperationAndGetLiteralTypes((Operation) operation.rhs, list);
        }

        if (list.contains(null)) {
            list.remove(null); // Wanneer een niet-assigned variabele wordt gebruikt komt expressiontype = null hierin.
        }
    }

    private boolean checkIfAllowedPropertyExpressionTypeCombination(ExpressionType expressionType, String propertyName) {
        if ((propertyName.equals("width") || propertyName.equals("height")) && allowedSizeExpressions.contains(expressionType)) { //
            return true;
        } else if ((propertyName.equals("background-color") || propertyName.equals("color")) && allowedColorExpressions.contains(expressionType)) {//
            return true;
        }
        return false;
    }

    private Set<ExpressionType> createSetOfExpressionTypesForSize() {
        Set<ExpressionType> sizeRelatedTypes = new HashSet<ExpressionType>();
        sizeRelatedTypes.add(ExpressionType.SCALAR);
        sizeRelatedTypes.add(ExpressionType.PIXEL);
        sizeRelatedTypes.add(ExpressionType.PERCENTAGE);
        return sizeRelatedTypes;
    }

    private Set<ExpressionType> createSetOfExpressionTypesForColors() {
        Set<ExpressionType> colorRelatedTypes = new HashSet<ExpressionType>();
        colorRelatedTypes.add(ExpressionType.COLOR);
        return colorRelatedTypes;
    }

    private void addExpressionTypeOfReferenceToList(VariableReference ref, Set<ExpressionType> list) {
        for (HashMap<String, ExpressionType> maps : variableTypes) {
            if (maps.containsKey(ref.name)) {
                ExpressionType type = maps.get(ref.name);
                list.add(type);
            }
        }
    }
}
