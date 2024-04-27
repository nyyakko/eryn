package org.nyyakko.eryn;

import org.nyyakko.eryn.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Lexer
{
    private static final List<Character> special = List.of('"', '(', ')', ':', '=', ' ');
    public static final List<String> keywords   = List.of(
        "def",
        "end",
        "if",
        "else",
        "switch",
        "case",
        "default",
        "break",
        "return",
        "let",
        "true",
        "false",
        "bool",
        "char",
        "int",
        "float",
        "double",
        "string"
    );

    public Lexer(ArrayList<String> source)
    {
        this.source    = source;
        this.cursorRow = 0;
        this.cursorCol = 0;
    }

    public ArrayList<String> getSource() { return source; }

    public ArrayList<Token> tokenize()
    {
        ArrayList<Token> result = new ArrayList<Token>();

        for (; cursorRow < source.size(); cursorRow += 1)
        {
            while (cursorCol < source.get(cursorRow).length())
            {
                dropSpaces();
                Integer prev = this.cursorCol;
                Token token  = nextToken();
                token.row    = cursorRow + 1;
                token.col    = prev + 1;
                result.add(token);
            }

            this.cursorCol = 0;
        }

        return result;
    }

    private Token nextToken()
    {
        Optional<Token> token =
            nextKeyword()
                .or(() -> nextIdentifier())
                .or(() -> nextStringLiteral())
                .or(() -> nextNumberLiteral())
                .or(() -> nextSpecial());

        assert(token.isPresent());

        return token.get();
    }

    private Optional<Token> nextStringLiteral()
    {
        if (peek() != '"') return Optional.empty();

        Token result     = new Token();
        StringBuilder sb = new StringBuilder();

        take();
        while (!eof() && peek() != '"')
        {
            sb.append(take());
        }

        if (peek() == '"') take();

        result.type = Token.Type.LITERAL;
        result.data = sb.toString();

        return Optional.of(result);
    }

    private Optional<Token> nextNumberLiteral()
    {
        if (!Character.isDigit(peek())) return Optional.empty();

        Token result     = new Token();
        StringBuilder sb = new StringBuilder();

        while (!eof() && !special.contains(peek()))
        {
            sb.append(take());
        }

        result.type = Token.Type.LITERAL;
        result.data = sb.toString();

        return Optional.of(result);
    }

    private Optional<Token> nextKeyword()
    {
        if (!Character.isAlphabetic(peek())) return Optional.empty();

        Token result     = new Token();
        StringBuilder sb = new StringBuilder();

        while (!eof() && !special.contains(peek()))
        {
            sb.append(take());
        }

        if (!keywords.contains(sb.toString()))
        {
            untake(sb.toString().length());
            return Optional.empty();
        }

        result.type = Token.Type.KEYWORD;
        result.data = sb.toString();

        return Optional.of(result);
    }

    private Optional<Token> nextIdentifier()
    {
        if (!Character.isAlphabetic(peek())) return Optional.empty();

        Token result     = new Token();
        StringBuilder sb = new StringBuilder();

        while (!eof() && !special.contains(peek()))
        {
            sb.append(take());
        }

        if (keywords.contains(sb.toString()))
        {
            untake(sb.toString().length());
            return Optional.empty();
        }

        result.type = Token.Type.IDENTIFIER;
        result.data = sb.toString();

        return Optional.of(result);
    }

    private Optional<Token> nextSpecial()
    {
        if (!special.contains(peek())) return Optional.empty();

        Token result     = new Token();
        StringBuilder sb = new StringBuilder();

        if      (peek() == '(') result.type = Token.Type.LPAREN;
        else if (peek() == ')') result.type = Token.Type.RPAREN;
        else if (peek() == ':') result.type = Token.Type.COLON;
        else if (peek() == '=') result.type = Token.Type.EQUALS;

        sb.append(take());
        result.data = sb.toString();

        return Optional.of(result);
    }

    private void dropSpaces() { while (!eof() && Character.isSpaceChar(peek())) take(); }
    private Boolean eof() { return cursorCol >= source.get(cursorRow).length(); }
    private Character peek() { return source.get(cursorRow).charAt(cursorCol); }
    private Character take() { return source.get(cursorRow).charAt(cursorCol++); }
    private void untake(int count) { cursorCol -= count; }

    private ArrayList<String> source;
    private Integer cursorRow;
    private Integer cursorCol;
}
