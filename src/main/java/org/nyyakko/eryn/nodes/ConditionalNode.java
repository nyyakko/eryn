package org.nyyakko.eryn.nodes;

import org.nyyakko.eryn.*;

import java.util.Optional;

public class ConditionalNode implements IStatement
{
    public INode.Type getNodeType() { return INode.Type.STATEMENT; }
    public IStatement.Type getStatementType() { return IStatement.Type.CONDITIONAL; }

    public INode condition;
    public INode ifBranch;
    public Optional<INode> elseBranch = Optional.empty();
}
