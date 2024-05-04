package org.nyyakko.eryn;

import org.nyyakko.eryn.Lexer;
import org.nyyakko.eryn.Nodes;
import org.nyyakko.eryn.Parser;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class Interpreter
{
    class Context
    {
        Context(
            ArrayList<Nodes.INode> nodes,
            HashMap<String, Nodes.INode> functionNodes,
            HashMap<String, Nodes.INode> variableNodes,
            HashMap<String, Nodes.INode> importNodes)
        {
            this.nodes         = nodes;
            this.functionNodes = functionNodes;
            this.variableNodes = variableNodes;
            this.importNodes   = importNodes;
        }

        public ArrayList<Nodes.INode> nodes;
        public HashMap<String, Nodes.INode> functionNodes;
        public HashMap<String, Nodes.INode> variableNodes;
        public HashMap<String, Nodes.INode> importNodes;
    }

    public Interpreter(Nodes.INode ast)
    {
        this.ast = ast;
    }

    public void interpret()
    {
        assert(getScopeContext((Nodes.Scope)ast).functionNodes.containsKey("main")) : "MAIN PROCEDURE NOT FOUND";

        Nodes.Function main = (Nodes.Function)(((Nodes.Scope)ast).functionNodes.get("main"));

        interpret(
            main.body,
            getScopeContext((Nodes.Scope)main.body),
            getScopeContext((Nodes.Scope)ast)
        );
    }

    private Context getScopeContext(Nodes.Scope scope)
    {
        for (String importPath : scope.importNodes.keySet())
        {
            assert(importPath.startsWith("\"") && importPath.endsWith("\""));

            scope.importNodes.put(importPath,
                Lexer.readSourceContent(new File(importPath.substring(1, importPath.length() - 1)))
                    .map((source) -> {
                        Lexer lexer = new Lexer(source);
                        return lexer.tokenize();
                    })
                    .map((tokens) -> {
                        Parser parser = new Parser(tokens);
                        return parser.parse();
                    })
                    .orElseThrow()
            );
        }

        return new Context(scope.nodes, scope.functionNodes, scope.variableNodes, scope.importNodes);
    }

    private void interpret(Nodes.INode head, Context localContext, Context globalContext)
    {
        switch (head)
        {
        case Nodes.IStatement statement -> {
            switch (statement)
            {
            case Nodes.Conditional node -> { assert(false) : String.format("(%d, %d) NOT IMPLEMENTED", node.position.row(), node.position.col()); }
            case Nodes.Selection node -> { assert(false) : String.format("(%d, %d) NOT IMPLEMENTED", node.position.row(), node.position.col()); }
            case Nodes.FunctionCall node -> {
                if (node.callType == Nodes.FunctionCall.Type.INTRINSIC)
                    interpretIntrinsicFunctionCall(node, localContext, globalContext);
                else
                    interpretFunctionCall(node, localContext, globalContext);
                break;
            }
            case Nodes.IStatement node -> { assert(false) : String.format("(%d, %d) INVALID NODE \"%s\" WAS REACHED", node.position.row(), node.position.col(), node); }
            }
            break;
        }
        case Nodes.Scope _ -> {
            for (Nodes.INode node : localContext.nodes)
                interpret(node, localContext, globalContext);
            break;
        }
        case Nodes.INode node -> { assert(false) : String.format("(%d, %d) INVALID NODE \"%s\" WAS REACHED", node.position.row(), node.position.col(), node); }
        }
    }

    private Nodes.Variable unwrapVariable(Nodes.INode head)
    {
        switch (head)
        {
        case Nodes.Let let: { return (Nodes.Variable)let.value; }
        case Nodes.Variable variable: { return variable; }
        case Nodes.INode node: { assert(false) : String.format("(%d, %d) INVALID NODE \"%s\" WAS REACHED", node.position.row(), node.position.col(), node); }
        }

        return null;
    }

    private Nodes.Function findFunction(Nodes.FunctionCall node, Context globalContext)
    {
        if (globalContext.functionNodes.containsKey(node.callee))
            return (Nodes.Function)globalContext.functionNodes.get(node.callee);

        for (String importPath : globalContext.importNodes.keySet())
        {
            Context importContext = getScopeContext((Nodes.Scope)globalContext.importNodes.get(importPath));
            if (importContext.functionNodes.containsKey(node.callee))
                return (Nodes.Function)importContext.functionNodes.get(node.callee);
        }

        return null;
    }

    private Nodes.Variable findVariable(String name, Context localContext, Context globalContext)
    {
        if (localContext.variableNodes.containsKey(name))
            return unwrapVariable(localContext.variableNodes.get(name));
        else
            return unwrapVariable(globalContext.variableNodes.get(name));
    }

    private void interpretIntrinsicFunctionCall(Nodes.FunctionCall node, Context localContext, Context globalContext)
    {
        Function<String, String> fnUnquoted = (string) -> {
            return string.startsWith("\"") && string.endsWith("\"")
                        ? string.substring(1, string.length() - 1)
                        : string;
        };

        switch (node.callee)
        {
        case "println": {
            String parameterName = node.arguments.get(0);

            Boolean isLocal  = localContext.variableNodes.containsKey(parameterName);
            Boolean isGlobal = globalContext.variableNodes.containsKey(parameterName);

            if (!(isLocal || isGlobal))
            {
                System.out.println(parameterName);
                break;
            }

            Nodes.Variable parameterNode = findVariable(parameterName, localContext, globalContext);
            System.out.println(fnUnquoted.apply(parameterNode.value.get()));

            break;
        }
        case "print": {
            String parameterName = node.arguments.get(0);

            Boolean isLocal  = localContext.variableNodes.containsKey(parameterName);
            Boolean isGlobal = globalContext.variableNodes.containsKey(parameterName);

            if (!(isLocal || isGlobal))
            {
                System.out.println(parameterName);
                break;
            }

            Nodes.Variable parameterNode = findVariable(parameterName, localContext, globalContext);
            System.out.print(fnUnquoted.apply(parameterNode.value.get()));

            break;
        }
        default: {
            assert(false)
                : String.format(
                    "(%d, %d) UNKNOWN INTRINSIC CALL \"%s\"",
                    node.position.row(),
                    node.position.col(),
                    node.callee
                  );
        }
        }
    }

    private void interpretFunctionCall(Nodes.FunctionCall node, Context localContext, Context globalContext)
    {
        Nodes.Function callee = findFunction(node, globalContext);

        assert(callee != null)
            : String.format(
                    "(%d, %d) FUNCTION \"%s\" IS NOT DEFINED",
                    node.position.row(),
                    node.position.col(),
                    node.callee
              );

        for (String parameterName : node.arguments)
        {
            Boolean isLocal  = localContext.variableNodes.containsKey(parameterName);
            Boolean isGlobal = globalContext.variableNodes.containsKey(parameterName);

            if (!(isLocal || isGlobal))
            {
                Nodes.Variable argumentNode = (Nodes.Variable)callee.arguments.get(node.arguments.indexOf(parameterName));
                // FIXME:
                //    * properly deduce types
                //    * properly handle type mismatch
                argumentNode.value = Optional.of(parameterName);
                ((Nodes.Scope)callee.body).variableNodes.put(argumentNode.name, argumentNode);
                continue;
            }

            Nodes.Variable argumentNode  = (Nodes.Variable)callee.arguments.get(node.arguments.indexOf(parameterName));
            Nodes.Variable parameterNode = findVariable(parameterName, localContext, globalContext);
            assert(argumentNode.type.equals(parameterNode.type))
                : String.format(
                        "(%d, %d) MISMATCHING TYPES EXPECTED \"%s\" GOT \"%s\"",
                        node.position.row(),
                        node.position.col(),
                        argumentNode.type,
                        parameterNode.type
                  );
            ((Nodes.Scope)callee.body).variableNodes.put(argumentNode.name, parameterNode);
        }

        interpret(callee.body, getScopeContext((Nodes.Scope)callee.body), globalContext);
    }

    private Nodes.INode ast;
}
