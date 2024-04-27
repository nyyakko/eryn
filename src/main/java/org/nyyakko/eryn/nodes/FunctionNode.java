package org.nyyakko.eryn.nodes;

import org.nyyakko.eryn.*;

import java.util.Optional;
import java.util.ArrayList;

public class FunctionNode extends ISymbol
{
    public ISymbol.Type getSymbolType() { return ISymbol.Type.FUNCTION; }

    public Optional<String> returnType = Optional.empty();
    public String name;
    public ArrayList<INode> arguments;
    public INode body;
}
