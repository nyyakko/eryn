package org.nyyakko.eryn;

import org.nyyakko.eryn.Lexer;
import org.nyyakko.eryn.nodes.*;

import java.util.ArrayList;
import java.util.Optional;

public class Parser
{
    class Context
    {
        public Context() {}

        public Context(Integer parent, Integer child)
        {
            this.parent = parent;
            this.child  = child;
        }

        Integer parent = 0;
        Integer child  = 0;
    }

    public Parser(ArrayList<Token> tokens)
    {
        this.tokens = tokens;
    }

    public INode parse() { return parse(new Context()); }

    private INode parse(Context context)
    {
        INode root = new ScopeNode();

        while (!eof())
        {
            Token currentToken = take();

            switch (currentToken.type)
            {
            case KEYWORD: {
                if (currentToken.data.equals("end") || (context.child > context.parent && (peek().data == "else" || peek().data == "default")))
                {
                    untake();
                    return root;
                }

                if (currentToken.data.equals("def"))
                {
                    INode node = parseFunction(context);
                    ((ScopeNode)root).functionNodes.put(((FunctionDeclNode)node).name, node);
                }
                else if (currentToken.data.equals("let"))
                {
                    INode node = parseLetStatement(context);
                    ((ScopeNode)root).variableNodes.put(((VariableNode)node).name, node);
                }
                else if (currentToken.data.equals("if"))
                {
                    INode node = parseIfStatement(context);
                    ((ScopeNode)root).nodes.add(node);
                }
                else if (currentToken.data.equals("return"))
                {
                    INode node = parseReturnStatement(context);
                    ((ScopeNode)root).nodes.add(node);
                }
                else
                {
                    assert(false)
                        : String.format("UNEXPECTED KEYWORD \"%s\" @ (%d,%d)",
                                currentToken.data, currentToken.row, currentToken.col);
                }

                break;
            }
            case LITERAL: {
                LiteralNode node = new LiteralNode();
                node.type        = null; // FIXME: properly deduce literal type
                node.value       = currentToken.data;
                return node;
            }
            case LPAREN: {
                ExpressionNode node = new ExpressionNode();
                node.value          = parse(context);
                assert(!eof() && peek().type == Token.Type.RPAREN);
                take();
                return node;
            }
            case IDENTIFIER: {
                untake();
                ((ScopeNode)root).nodes.add(parseFunctionCall(context));
                break;
            }

            case RPAREN:
            case COLON:
            case BEGIN__:
            case END__:
            default: {
                assert(false)
                    : String.format("UNEXPECTED TOKEN \"%s\" @ (%d,%d)",
                            currentToken.data, currentToken.row, currentToken.col);
                break;
            }
            }
        }

        return root;
    }

    private INode parseVariable(Context context)
    {
        VariableNode node = new VariableNode();
        assert(!Lexer.keywords.contains(peek().data)) : System.out.printf("Something fishy at (%d,%d)%n", peek().row, peek().col);;
        node.name = take().data;
        take();
        node.type = take().data;

        if (peek().type == Token.Type.EQUALS)
        {
            take();
            assert(!Lexer.keywords.contains(peek().data)) : System.out.printf("Something fishy at (%d,%d)%n", peek().row, peek().col);;
            node.value = Optional.of(take().data);
        }

        return node;
    }

    private INode parseIfStatement(Context context)
    {
        ConditionalNode node = new ConditionalNode();

        node.condition   = parse(context);
        node.ifBranch    = parse(new Context(context.child, context.child + 1));
        INode elseBranch = parse(new Context(context.child, context.parent));
        node.elseBranch  = elseBranch != null ? Optional.of(elseBranch) : Optional.empty();

        assert(peek().type == Token.Type.KEYWORD && peek().data.equals("end")) : System.out.printf("Something fishy at (%d,%d)%n", peek().row, peek().col);;
        take();

        return node;
    }

    private INode parseLetStatement(Context context)
    {
        // untake();
        // VariableDeclNode node = new VariableDeclNode();
        // node.token = take();
        // node.value = parseVariable(context);

        // return node;
        return parseVariable(context);
    }

    private INode parseFunction(Context context)
    {
        FunctionDeclNode node = new FunctionDeclNode();
        node.returnType   = Optional.empty(); // FIXME: properly handle return type
        assert(!Lexer.keywords.contains(peek().data)) : System.out.printf("Something fishy at (%d,%d)%n", peek().row, peek().col);
        node.name         = take().data;
        node.arguments    = new ArrayList<INode>();

        take();
        while (!eof() && peek().type != Token.Type.RPAREN)
        {
            node.arguments.add(parseVariable(context));
        }
        take();

        if (peek().data.charAt(0) == ':')
        {
            take();
            assert(peek().type == Token.Type.KEYWORD) : System.out.printf("Something fishy at (%d,%d)%n", peek().row, peek().col);
            node.returnType = Optional.of(take().data);
        }

        node.body = parse(new Context(context.parent, context.child + 1));

        assert(!eof() && peek().type == Token.Type.KEYWORD && peek().data.equals("end")) : System.out.printf("Something fishy at (%d,%d)%n", peek().row, peek().col);;
        take();

        return node;
    }

    private INode parseFunctionCall(Context context)
    {
        FunctionCallNode node = new FunctionCallNode();
        node.callee           = take().data;
        node.arguments        = new ArrayList<String>();

        take();
        while (!eof() && peek().type != Token.Type.RPAREN)
        {
            // FIXME: Token.Type.IDENTIFIER includes function names, which should not be printable. ig.
            assert(peek().type == Token.Type.LITERAL || peek().type == Token.Type.IDENTIFIER) : System.out.printf("Something fishy at (%d,%d)%n", peek().row, peek().col);;
            node.arguments.add(take().data);
        }
        take();

        return node;
    }

    private INode parseReturnStatement(Context context)
    {
        assert(false) : "UNIMPLEMENTED";
        return null;
    }

    private Token peek() { return tokens.get(this.cursor); }
    private Token take() { return tokens.get(this.cursor++); }
    private void untake() { cursor--; }
    private Boolean eof() { return tokens.size() == this.cursor; }

    private ArrayList<Token> tokens;
    private Integer cursor = 0;
}
