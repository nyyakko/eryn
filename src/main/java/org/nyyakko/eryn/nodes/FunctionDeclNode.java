package org.nyyakko.eryn.nodes;

import org.nyyakko.eryn.*;

import java.util.Optional;
import java.util.ArrayList;

public class FunctionDeclNode implements ISymbol
{
    public INode.Type getNodeType() { return INode.Type.SYMBOL; }
    public ISymbol.Type getSymbolType() { return ISymbol.Type.FUNCTION; }

    public Optional<String> returnType = Optional.empty();
    public String name;
    public ArrayList<INode> arguments;
    public INode body;
}
