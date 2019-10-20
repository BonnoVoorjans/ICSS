package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;

public class Generator {

	public String generate(AST ast) {
		StringBuilder string = new StringBuilder();
        for(ASTNode node : ast.root.getChildren()){
			string.append(traverseTree(node));
		}
		return string.toString();
	}

	public String traverseTree(ASTNode node){
		StringBuilder string = new StringBuilder();
		string.append(node.toString()).append(" ");
		return string.toString();
	}
}
