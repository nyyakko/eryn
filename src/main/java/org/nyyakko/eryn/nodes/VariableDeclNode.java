package org.nyyakko.eryn.nodes;

import org.nyyakko.eryn.*;

public class VariableDeclNode implements IStatement
{
    public INode.Type getNodeType() { return INode.Type.STATEMENT; }
    public IStatement.Type getStatementType() { return IStatement.Type.DECLARATION; }

    public Token token;
    public INode value;
}
