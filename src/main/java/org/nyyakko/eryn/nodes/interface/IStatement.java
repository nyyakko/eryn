package org.nyyakko.eryn.nodes;

public interface IStatement extends INode
{
    enum Type
    {
        BEGIN__,
        EXPRESSION,
        DECLARATION,
        CONDITIONAL,
        END__
    }

    public Type getStatementType();
}
