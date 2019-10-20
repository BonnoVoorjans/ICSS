package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;

public class RemoveIf implements Transform {



    @Override
    public void apply(AST ast) {
        for(ASTNode node : ast.root.getChildren()){
            if(node instanceof Stylerule){
                evaluateIfStatements(node);
            }
        }
    }

    private void evaluateIfStatements(ASTNode parent){
        for(ASTNode node : parent.getChildren()){
            if(node instanceof IfClause){
                evaluateIfStatements(node);
                BoolLiteral boolLit = (BoolLiteral)((IfClause) node).conditionalExpression;
                if(boolLit.value == true) {
                    for (ASTNode child : node.getChildren()) {
                        if (child instanceof Declaration) {
                            parent.addChild(child);
                        }
                    }
                }
                parent.removeChild(node);
            }
        }
    }
}
