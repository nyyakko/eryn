package org.nyyakko.eryn.nodes;

public interface INode
{
    enum Type
    {
        BEGIN__,
        SYMBOL,
        SCOPE,
        STATEMENT,
        LITERAL,
        EXPRESSION,
        END__
    }

    public Type getNodeType();
}
