package org.nyyakko.eryn.nodes;

import org.nyyakko.eryn.*;

import java.util.ArrayList;
import java.util.HashMap;

public class FunctionCallNode extends IStatement
{
    public IStatement.Type getStatementType() { return IStatement.Type.EXPRESSION; }

    public String callee;
    public ArrayList<String> arguments;
}
