package org.nyyakko.eryn;

import org.nyyakko.eryn.Token;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Lexer
{
    public static Optional<ArrayList<String>> readSourceContent(File path)
    {
        Optional<ArrayList<String>> result = Optional.empty();

        try (Scanner scanner = new Scanner(new BufferedReader(new FileReader(path))))
        {
            ArrayList<String> source = new ArrayList<String>();
            while (scanner.hasNext())
                source.add(scanner.nextLine());
            result = Optional.of(source);
        }
        catch (IOException exception)
        {
            System.out.printf("%s%n", exception.getMessage());
        }

        return result;
    }

    static class Position
    {
        public Position(Integer row, Integer col)
        {
            this.row = row;
            this.col = col;
        }

        public Integer row;
        public Integer col;
    }

    private static final List<Character> special = List.of('"', '(', ')', ':', '=', ' ');
    public static final List<String> keywords    = List.of(
        "fn",
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
        "string",
        "intrinsic",
        "import"
    );

    public Lexer(ArrayList<String> source)
    {
        this.source = source;
        this.cursor = new Position(0, 0);
    }

    public ArrayList<String> getSource() { return source; }

    public ArrayList<Token> tokenize()
    {
        ArrayList<Token> result = new ArrayList<Token>();

        for (; cursor.row < source.size(); cursor.row += 1)
        {
            while (cursor.col < source.get(cursor.row).length())
            {
                dropSpaces();
                Integer prev   = cursor.col;
                Token token    = nextToken().get();
                token.position = new Token.Position(cursor.row + 1, prev + 1);
                result.add(token);
            }

            cursor.col = 0;
        }

        return result;
    }

    private Optional<Token> nextToken()
    {
        return nextKeyword()
                .or(() -> nextIdentifier())
                .or(() -> nextStringLiteral())
                .or(() -> nextNumberLiteral())
                .or(() -> nextSpecial());
    }

    private Optional<Token> nextStringLiteral()
    {
        if (peek() != '"') return Optional.empty();

        Token result     = new Token();
        StringBuilder sb = new StringBuilder();

        take();
        while (!eof())
        {
            if (peek() == '"') { take(); break; }
            sb.append(take());
        }

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
    private Boolean eof() { return cursor.col >= source.get(cursor.row).length(); }
    private Character peek() { return source.get(cursor.row).charAt(cursor.col); }
    private Character take() { return source.get(cursor.row).charAt(cursor.col++); }
    private void untake(int count) { cursor.col -= count; }

    private ArrayList<String> source;
    private Position cursor;
}
