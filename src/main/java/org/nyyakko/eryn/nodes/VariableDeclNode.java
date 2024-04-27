package org.nyyakko.eryn.nodes;

import org.nyyakko.eryn.*;

public class VariableDeclNode extends IStatement
{
    public IStatement.Type getStatementType() { return IStatement.Type.DECLARATION; }

    public Token token;
    public INode value;
}
