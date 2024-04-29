package org.nyyakko.eryn;

import java.util.ArrayList;
import java.util.Optional;
import java.util.HashMap;

public class Nodes
{
    static sealed abstract class INode
    {
        static public record Position(Integer row, Integer col) {}

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

        public Position position;
    }

    static final class Scope extends INode
    {
        public ArrayList<INode> nodes = new ArrayList<INode>();
        public HashMap<String, INode> functionNodes = new HashMap<String, INode>();
        public HashMap<String, INode> variableNodes = new HashMap<String, INode>();
    }

    static final class Literal extends INode
    {
        public String type;
        public String value;
    }

    static final class Expression extends INode
    {
        public INode value;
    }

    // ----------------------------------------------------- //

    static sealed abstract class ISymbol extends INode
    {
        static enum Type
        {
            BEGIN__,
            FUNCTION,
            VARIABLE,
            END__
        }
    }

    static final class Variable extends ISymbol
    {
        public String type;
        public String name;
        public Optional<String> value = Optional.empty();
    }

    static final class Function extends ISymbol
    {
        public Optional<String> returnType = Optional.empty();
        public String name;
        public ArrayList<INode> arguments;
        public INode body;
    }

    // ----------------------------------------------------- //

    static sealed abstract class IStatement extends INode
    {
        static enum Type
        {
            BEGIN__,
            EXPRESSION,
            DECLARATION,
            CONDITIONAL,
            END__
        }
    }

    static final class Conditional extends IStatement
    {
        public INode condition;
        public INode ifBranch;
        public Optional<INode> elseBranch = Optional.empty();
    }

    static final class FunctionCall extends IStatement
    {
        public String callee;
        public ArrayList<String> arguments;
    }

    static final class Let extends IStatement
    {
        INode value;
    }
}
