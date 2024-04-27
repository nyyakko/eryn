package org.nyyakko.eryn;

import org.nyyakko.eryn.nodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class Interpreter
{
    class Context
    {
        Context(ArrayList<INode> nodes, HashMap<String, INode> functionNodes, HashMap<String, INode> variableNodes)
        {
            this.nodes         = nodes;
            this.functionNodes = functionNodes;
            this.variableNodes = variableNodes;
        }

        public ArrayList<INode> nodes;
        public HashMap<String, INode> functionNodes;
        public HashMap<String, INode> variableNodes;
    }

    Interpreter(INode ast)
    {
        this.ast = ast;
    }

    public void interpret()
    {
        assert(getScopeContext((ScopeNode)ast).functionNodes.containsKey("main")) : "MAIN PROCEDURE NOT FOUND";

        FunctionNode main = (FunctionNode)(((ScopeNode)ast).functionNodes.get("main"));

        interpret(
            main.body,
            getScopeContext((ScopeNode)ast),
            getScopeContext((ScopeNode)main.body)
        );
    }

    private void interpret(INode head, Context globalContext, Context localContext)
    {
        switch (head.getNodeType())
        {
        case SCOPE: {
            for (INode node : localContext.nodes)
            {
                interpret(node, globalContext, localContext);
            }

            break;
        }
        case SYMBOL: break;
        case STATEMENT: {
            IStatement statement = (IStatement)head;

            switch (statement.getStatementType())
            {
            case EXPRESSION: {
                FunctionCallNode node = (FunctionCallNode)statement;

                if (node.callee.equals("println")) // NOTE: das crazy
                {
                    if (localContext.variableNodes.containsKey(node.arguments.get(0)))
                        System.out.println(((VariableNode)localContext.variableNodes.get(node.arguments.get(0))).value.get());
                    else if (globalContext.variableNodes.containsKey(node.arguments.get(0)))
                        System.out.println(((VariableNode)globalContext.variableNodes.get(node.arguments.get(0))).value.get());
                    else
                        System.out.println(node.arguments.get(0));
                }
                else
                {
                    FunctionNode callee = (FunctionNode)globalContext.functionNodes.get(node.callee);

                    for (String parameterName : node.arguments)
                    {
                        if (localContext.variableNodes.containsKey(parameterName))
                        {
                            VariableNode argumentNode  = (VariableNode)callee.arguments.get(node.arguments.indexOf(parameterName));
                            VariableNode parameterNode = (VariableNode)localContext.variableNodes.get(parameterName);
                            assert(argumentNode.type.equals(parameterNode.type)) : "MISMATCHING TYPES";
                            argumentNode.value = parameterNode.value;
                            ((ScopeNode)callee.body).variableNodes.put(argumentNode.name, argumentNode);
                        }
                        else if (globalContext.variableNodes.containsKey(parameterName))
                        {
                            VariableNode argumentNode  = (VariableNode)callee.arguments.get(node.arguments.indexOf(parameterName));
                            VariableNode parameterNode = (VariableNode)globalContext.variableNodes.get(parameterName);
                            assert(argumentNode.type.equals(parameterNode.type)) : "MISMATCHING TYPES";
                            argumentNode.value = parameterNode.value;
                            ((ScopeNode)callee.body).variableNodes.put(argumentNode.name, argumentNode);
                        }
                        else
                        {
                            VariableNode argumentNode  = (VariableNode)callee.arguments.get(node.arguments.indexOf(parameterName));
                            VariableNode parameterNode = (VariableNode)globalContext.variableNodes.get(parameterName);
                            // FIXME:
                            //    * properly deduce types
                            //    * properly handle type mismatch
                            argumentNode.value         = Optional.of(parameterName);
                            ((ScopeNode)callee.body).variableNodes.put(argumentNode.name, argumentNode);
                        }
                    }

                    interpret(callee.body, globalContext, getScopeContext((ScopeNode)callee.body));
                }

                break;
            }
            case DECLARATION: break;
            case CONDITIONAL: break;

            case BEGIN__:
            case END__: {
                assert(false);
                break;
            }
            }

            break;
        }
        case LITERAL: break;
        case EXPRESSION: break;

        case BEGIN__:
        case END__: {
            assert(false);
            break;
        }
        }
    }

    private Context getScopeContext(ScopeNode scope)
    {
        return new Context(scope.nodes, scope.functionNodes, scope.variableNodes);
    }

    private INode ast;
}
