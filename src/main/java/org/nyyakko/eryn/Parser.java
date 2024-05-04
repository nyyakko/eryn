package org.nyyakko.eryn;

import org.nyyakko.eryn.Token;
import org.nyyakko.eryn.Lexer;
import org.nyyakko.eryn.Nodes;

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

    public Nodes.INode parse() { return parse(new Context()); }

    private Nodes.INode parse(Context context)
    {
        Nodes.INode root = new Nodes.Scope();

        while (!eof())
        {
            Token token = take();

            switch (token.type)
            {
            case KEYWORD: {
                if (token.data.equals("end") || (context.child > context.parent && (peek().data == "else" || peek().data == "default")))
                {
                    untake();
                    return root;
                }

                if (token.data.equals("intrinsic")) { break; }

                Nodes.INode node = null;

                switch (token.data)
                {
                case "fn": {
                    node = parseFunction(context);
                    ((Nodes.Scope)root).functionNodes.put(((Nodes.Function)node).name, node);
                    break;
                }
                case "let": {
                    node = parseLetStatement(context);
                    ((Nodes.Scope)root).variableNodes.put(((Nodes.Variable)((Nodes.Let)node).value).name, node);
                    break;
                }
                case "import": {
                    node = parseImportStatement(context);
                    ((Nodes.Scope)root).importNodes.put(((Nodes.Import)node).importee, node);
                    break;
                }
                default: {
                    assert(false)
                        : String.format(
                              "(%d, %d) UNEXPECTED KEYWORD \"%s\" WAS REACHED",
                              token.position.row(),
                              token.position.col(),
                              token.data
                          );
                }
                }

                node.position = new Nodes.INode.Position(token.position.row(), token.position.col());

                break;
            }
            case LITERAL: {
                Nodes.Literal node = new Nodes.Literal();
                node.type          = null; // FIXME: properly deduce literal type
                node.value         = token.data;
                node.position      = new Nodes.INode.Position(token.position.row(), token.position.col());
                return node;
            }
            case LPAREN: {
                Nodes.Expression node = new Nodes.Expression();
                node.value            = parse(context);
                node.position         = new Nodes.INode.Position(token.position.row(), token.position.col());
                assert(!eof() && peek().type == Token.Type.RPAREN);
                root = node;
            }
            case RPAREN: { return root; }
            case IDENTIFIER: {
                untake();
                untake();

                Nodes.INode node = null;

                if (peek().type == Token.Type.KEYWORD && peek().data.equals("intrinsic"))
                {
                    take();
                    node = parseFunctionCall(context, Nodes.FunctionCall.Type.INTRINSIC);
                }
                else
                {
                    take();
                    node = parseFunctionCall(context, Nodes.FunctionCall.Type.NONINTRINSIC);
                }

                node.position = new Nodes.INode.Position(token.position.row(), token.position.col());
                ((Nodes.Scope)root).nodes.add(node);
                break;
            }

            case COLON:
            case BEGIN__:
            case END__:
            default: {
                assert(false)
                    : String.format(
                            "(%d, %d) UNEXPECTED TOKEN \"%s\" WAS REACHED",
                            token.position.row(),
                            token.position.col(),
                            token.data
                      );
                break;
            }
            }
        }

        return root;
    }

    private Nodes.INode parseVariable(Context context)
    {
        Nodes.Variable node = new Nodes.Variable();
        assert(!Lexer.keywords.contains(peek().data))
                : String.format("(%d, %d) \"%s\" IS A RESERVED IDENTIFIER", peek().position.row(), peek().position.col(), peek().data);
        node.name = take().data;
        take();
        node.type = take().data;

        if (peek().type == Token.Type.EQUALS)
        {
            take();
            assert(!Lexer.keywords.contains(peek().data))
                : String.format("(%d, %d) \"%s\" IS A RESERVED IDENTIFIER", peek().position.row(), peek().position.col(), peek().data);
            node.value = Optional.of(take().data);
        }

        return node;
    }

    private Nodes.INode parseIfStatement(Context context)
    {
        Nodes.Conditional node = new Nodes.Conditional();

        node.condition   = parse(context);
        node.ifBranch    = parse(new Context(context.child, context.child + 1));
        Nodes.INode elseBranch = parse(new Context(context.child, context.parent));
        node.elseBranch  = elseBranch != null ? Optional.of(elseBranch) : Optional.empty();

        assert(peek().type == Token.Type.KEYWORD && peek().data.equals("end"))
            : String.format("(%d,%d) EXPECTED \"end\" GOT \"%s\"", peek().position.row(), peek().position.col(), peek().data);
        take();

        return node;
    }

    private Nodes.INode parseLetStatement(Context context)
    {
        Nodes.Let node = new Nodes.Let();
        node.value     = parseVariable(context);
        return node;
    }

    private Nodes.INode parseFunction(Context context)
    {
        Nodes.Function node = new Nodes.Function();
        node.returnType   = Optional.empty(); // FIXME: properly handle return type
        assert(!Lexer.keywords.contains(peek().data))
                : String.format("(%d, %d) \"%s\" IS A RESERVED IDENTIFIER", peek().position.row(), peek().position.col(), peek().data);
        node.name         = take().data;
        node.arguments    = new ArrayList<Nodes.INode>();

        take();
        while (!eof() && peek().type != Token.Type.RPAREN)
        {
            node.arguments.add(parseVariable(context));
        }
        take();

        if (peek().data.charAt(0) == ':')
        {
            take();
            assert(peek().type == Token.Type.KEYWORD)
                : String.format("(%d, %d) \"%s\" IS A RESERVED IDENTIFIER", peek().position.row(), peek().position.col(), peek().data);
            node.returnType = Optional.of(take().data);
        }

        node.body = parse(new Context(context.parent, context.child + 1));

        assert(!eof() && peek().type == Token.Type.KEYWORD && peek().data.equals("end"))
            : String.format("(%d,%d), EXPECTED \"end\" GOT \"%s\"", peek().position.row(), peek().position.col(), peek().data);
        take();

        return node;
    }

    private Nodes.INode parseFunctionCall(Context context, Nodes.FunctionCall.Type type)
    {
        Nodes.FunctionCall node = new Nodes.FunctionCall();
        node.callee             = take().data;
        node.arguments          = new ArrayList<String>();
        node.callType           = type;

        take();
        while (!eof() && peek().type != Token.Type.RPAREN)
        {
            // FIXME: Token.Type.IDENTIFIER also includes function names
            assert(peek().type == Token.Type.LITERAL || peek().type == Token.Type.IDENTIFIER)
                : String.format("(%d,%d) INVALID ARGUMENT", peek().position.row(), peek().position.col());
            node.arguments.add(take().data);
        }
        take();

        return node;
    }

    private Nodes.INode parseImportStatement(Context context)
    {
        Nodes.Import node = new Nodes.Import();
        node.importee     = take().data;
        return node;
    }

    private Nodes.INode parseReturnStatement(Context context)
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
