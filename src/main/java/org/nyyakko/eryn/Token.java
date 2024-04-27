package org.nyyakko.eryn;

public class Token
{
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
    public Integer row;
    public Integer col;
}
