package org.nyyakko.eryn.nodes;

public interface ISymbol extends INode
{
    enum Type
    {
        BEGIN__,
        FUNCTION,
        VARIABLE,
        END__
    }

    public Type getSymbolType();
}
