package org.nyyakko.eryn.nodes;

import org.nyyakko.eryn.*;

import java.util.Optional;

public class ConditionalNode extends IStatement
{
    public IStatement.Type getStatementType() { return IStatement.Type.CONDITIONAL; }

    public INode condition;
    public INode ifBranch;
    public Optional<INode> elseBranch = Optional.empty();
}
