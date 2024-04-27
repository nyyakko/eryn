package org.nyyakko.eryn.nodes;

import org.nyyakko.eryn.*;

import java.util.Optional;

public class VariableNode extends ISymbol
{
    public ISymbol.Type getSymbolType() { return ISymbol.Type.VARIABLE; }

    public String type;
    public String name;
    public Optional<String> value = Optional.empty();
}
