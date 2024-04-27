package org.nyyakko.eryn.nodes;

import org.nyyakko.eryn.*;

public class LiteralNode implements INode
{
    public Type getNodeType() { return INode.Type.LITERAL; }

    public String type;
    public String value;
}
