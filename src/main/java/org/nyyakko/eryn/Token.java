package org.nyyakko.eryn;

public class Token
{
    static public record Position(Integer row, Integer col) {}

    public enum Type
    {
        BEGIN__,
        KEYWORD,
        LITERAL,
        IDENTIFIER,
        LPAREN,
        RPAREN,
        COLON,
        EQUALS,
        END__
    }

    public Type type;
    public String data;
    public Position position;
}
