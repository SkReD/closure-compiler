package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;

import java.util.logging.Logger;

class InfomaximumStringRenames extends NodeTraversal.AbstractPostOrderCallback implements CompilerPass {

    private final Compiler compiler;

    InfomaximumStringRenames(Compiler compiler) {
        this.compiler = compiler;
    }

    public void visit(NodeTraversal t, Node n, Node parent) {
        
		Node nodeParent;
	    String newName;
		VariableMap map = compiler.getPropertyMap();
	     Logger logger =
			    Logger.getLogger(InfomaximumStringRenames.class.getName());

	    if (map == null)
	    {
		    return;
	    }

		if (!n.isString())
		{
			return;
		}

	    if (compiler.getCodingConvention().isLocalization(n))
	    {
		     return;
	    }

		newName = map.lookupNewName(n.getString());
		if (newName != null)
		{
            n.setString(newName);
			compiler.reportCodeChange();
		}
    }

    public void process(Node externs, Node root) {
        NodeTraversal.traverse(compiler, root, this);
    }

}
