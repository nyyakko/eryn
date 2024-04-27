package org.nyyakko.eryn.nodes;

import org.nyyakko.eryn.*;

import java.util.HashMap;
import java.util.ArrayList;

public class ScopeNode implements INode
{
    public Type getNodeType() { return INode.Type.SCOPE; }

    public ArrayList<INode> nodes = new ArrayList<INode>();
    public HashMap<String, INode> functionNodes = new HashMap<String, INode>();
    public HashMap<String, INode> variableNodes = new HashMap<String, INode>();
}
