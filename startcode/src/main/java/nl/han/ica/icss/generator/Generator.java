package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;

public class Generator {
    private final String LINESEPERATOR = System.lineSeparator();
    private final String SPACINGTAB = "\t";

    public String generate(AST ast) {
        StringBuilder string = new StringBuilder();
        for (ASTNode node : ast.root.getChildren()) {
            string.append(traverseTree(node));

        }
        return string.toString();
    }

    public String traverseTree(ASTNode node) {
        StringBuilder string = new StringBuilder();

        if (node instanceof Stylerule) {
            for (Selector selector : ((Stylerule) node).selectors) {
                string.append(selector.toString() + " ");
            }
            string.append('{' + LINESEPERATOR);


            for (ASTNode declaration : node.getChildren()) {
                if (declaration instanceof Declaration) {
                    string.append(SPACINGTAB);
                    string.append(((Declaration) declaration).property.name + ": ");
                    string.append(((Declaration) declaration).expression.toString());
                    string.append(';');
                    string.append(LINESEPERATOR);
                }
            }
            string.append('}' + LINESEPERATOR + LINESEPERATOR);
        }

        return string.toString();
    }
}
