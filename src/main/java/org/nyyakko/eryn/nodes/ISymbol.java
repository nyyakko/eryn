package org.nyyakko.eryn.nodes;

public abstract class ISymbol implements INode
{
    public enum Type
    {
        BEGIN__,
        FUNCTION,
        VARIABLE,
        END__
    }

    public INode.Type getNodeType() { return INode.Type.SYMBOL; }
    abstract public Type getSymbolType();
}
