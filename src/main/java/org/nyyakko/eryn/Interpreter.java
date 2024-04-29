package org.nyyakko.eryn;

import org.nyyakko.eryn.Nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

public class Interpreter
{
    class Context
    {
        Context(
            ArrayList<Nodes.INode> nodes,
            HashMap<String, Nodes.INode> functionNodes,
            HashMap<String, Nodes.INode> variableNodes)
        {
            this.nodes         = nodes;
            this.functionNodes = functionNodes;
            this.variableNodes = variableNodes;
        }

        public ArrayList<Nodes.INode> nodes;
        public HashMap<String, Nodes.INode> functionNodes;
        public HashMap<String, Nodes.INode> variableNodes;
    }

    static final List<String> intrinsics = List.of(
        "println"
    );

    Interpreter(Nodes.INode ast)
    {
        this.ast = ast;
    }

    public void interpret()
    {
        assert(getScopeContext((Nodes.Scope)ast).functionNodes.containsKey("main")) : "MAIN PROCEDURE NOT FOUND";

        Nodes.Function main = (Nodes.Function)(((Nodes.Scope)ast).functionNodes.get("main"));

        interpret(
            main.body,
            getScopeContext((Nodes.Scope)ast),
            getScopeContext((Nodes.Scope)main.body)
        );
    }

    private void interpret(Nodes.INode head, Context globalContext, Context localContext)
    {
        switch (head)
        {
        case Nodes.IStatement statement -> {
            switch (statement)
            {
            case Nodes.Conditional _ -> {
                assert(false) : "NOT IMPLEMENTED";
            }
            case Nodes.FunctionCall node -> {
                Function<Nodes.INode, Nodes.Variable> fnGetVariable = (x) -> {
                    switch (x)
                    {
                    case Nodes.Let let -> { return (Nodes.Variable)let.value; }
                    case Nodes.Variable variable -> { return variable; }
                    case Nodes.INode _ -> { break; }
                    }
                    assert(false);
                    return null;
                };

                if (intrinsics.contains(node.callee))
                {
                    System.out.println("");
                    handleIntrinsicCall(node, localContext, globalContext);
                    Boolean isLocal  = localContext.variableNodes.containsKey(node.arguments.get(0));
                    Boolean isGlobal = globalContext.variableNodes.containsKey(node.arguments.get(0));

                    if (!(isLocal || isGlobal))
                    {
                        System.out.println(node.arguments.get(0));
                        break;
                    }

                    if (isLocal)
                    {
                        Nodes.Variable variable = fnGetVariable.apply(localContext.variableNodes.get(node.arguments.get(0)));
                        System.out.println(variable.value.get());
                    }
                    else
                    {
                        Nodes.Variable variable = fnGetVariable.apply(globalContext.variableNodes.get(node.arguments.get(0)));
                        System.out.println(variable.value.get());
                    }

                    break;
                }
                else
                {
                    Nodes.Function callee = (Nodes.Function)globalContext.functionNodes.get(node.callee);

                    for (String parameterName : node.arguments)
                    {
                        Boolean isLocal  = localContext.variableNodes.containsKey(parameterName);
                        Boolean isGlobal = globalContext.variableNodes.containsKey(parameterName);

                        if (!(isLocal || isGlobal))
                        {
                            Nodes.Variable argumentNode  = (Nodes.Variable)callee.arguments.get(node.arguments.indexOf(parameterName));
                            Nodes.Variable parameterNode = (Nodes.Variable)globalContext.variableNodes.get(parameterName);
                            // FIXME:
                            //    * properly deduce types
                            //    * properly handle type mismatch
                            argumentNode.value         = Optional.of(parameterName);
                            ((Nodes.Scope)callee.body).variableNodes.put(argumentNode.name, argumentNode);
                            continue;
                        }

                        if (isLocal)
                        {
                            Nodes.Variable argumentNode  = (Nodes.Variable)callee.arguments.get(node.arguments.indexOf(parameterName));
                            Nodes.Let let = (Nodes.Let)localContext.variableNodes.get(parameterName);
                            Nodes.Variable parameterNode = (Nodes.Variable)let.value;
                            assert(argumentNode.type.equals(parameterNode.type)) : "MISMATCHING TYPES";
                            argumentNode.value = parameterNode.value;
                            ((Nodes.Scope)callee.body).variableNodes.put(argumentNode.name, argumentNode);
                        }
                        else
                        {
                            Nodes.Variable argumentNode  = (Nodes.Variable)callee.arguments.get(node.arguments.indexOf(parameterName));
                            Nodes.Variable parameterNode = (Nodes.Variable)globalContext.variableNodes.get(parameterName);
                            assert(argumentNode.type.equals(parameterNode.type)) : "MISMATCHING TYPES";
                            argumentNode.value = parameterNode.value;
                            ((Nodes.Scope)callee.body).variableNodes.put(argumentNode.name, argumentNode);
                        }
                    }

                    interpret(callee.body, globalContext, getScopeContext((Nodes.Scope)callee.body));
                }
                break;
            }
            case Nodes.IStatement _ -> { assert(false); }
            }
            break;
        }
        case Nodes.Scope _ -> {
            for (Nodes.INode node : localContext.nodes)
                interpret(node, globalContext, localContext);
            break;
        }
        case Nodes.INode _ -> { assert(false); }
        }
    }

    private Context getScopeContext(Nodes.Scope scope)
    {
        return new Context(scope.nodes, scope.functionNodes, scope.variableNodes);
    }

    private Nodes.INode ast;
}
