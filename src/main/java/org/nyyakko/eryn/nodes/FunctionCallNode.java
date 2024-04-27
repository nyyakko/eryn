package org.nyyakko.eryn.nodes;

import org.nyyakko.eryn.*;

import java.util.ArrayList;
import java.util.HashMap;

public class FunctionCallNode implements IStatement
{
    public INode.Type getNodeType() { return INode.Type.STATEMENT; }
    public IStatement.Type getStatementType() { return IStatement.Type.EXPRESSION; }

    public String callee;
    public ArrayList<String> arguments;
}
