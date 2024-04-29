package org.nyyakko.eryn;

import java.util.ArrayList;
import java.util.Optional;
import java.util.HashMap;

public class Nodes
{
    static sealed abstract class INode
    {
        static public record Position(Integer row, Integer col) {}
        public Position position;
    }

    static final class Scope extends INode
    {
        public ArrayList<INode> nodes = new ArrayList<INode>();
        public HashMap<String, INode> functionNodes = new HashMap<String, INode>();
        public HashMap<String, INode> variableNodes = new HashMap<String, INode>();
        public HashMap<String, INode> importNodes   = new HashMap<String, INode>();
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

    static sealed abstract class ISymbol extends INode {}

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

    static sealed abstract class IStatement extends INode {}

    static final class Conditional extends IStatement
    {
        public INode condition;
        public INode ifBranch;
        public Optional<INode> elseBranch = Optional.empty();
    }

    static final class Selection extends IStatement
    {
        public INode pattern;
        public ArrayList<INode> cases;
        public Optional<INode> defaultCase = Optional.empty();
    }

    static final class FunctionCall extends IStatement
    {
        static enum Type { INTRINSIC, NONINTRINSIC }

        public String callee;
        public ArrayList<String> arguments;
        public Type callType = Type.NONINTRINSIC;
    }

    static final class Let extends IStatement
    {
        public INode value;
    }

    static final class Import extends IStatement
    {
        public String importee;
        public INode scope;
    }
}
