package org.nyyakko.eryn.nodes;

import org.nyyakko.eryn.*;

public class ExpressionNode implements INode
{
    public Type getNodeType() { return INode.Type.EXPRESSION; }

    public INode value;
}
