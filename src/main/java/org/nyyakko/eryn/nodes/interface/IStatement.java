package org.nyyakko.eryn.nodes;

public abstract class IStatement implements INode
{
    public enum Type
    {
        BEGIN__,
        EXPRESSION,
        DECLARATION,
        CONDITIONAL,
        END__
    }

    public INode.Type getNodeType() { return INode.Type.STATEMENT; }
    abstract public Type getStatementType();
}
