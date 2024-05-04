package org.nyyakko.eryn;

import org.nyyakko.eryn.Lexer;
import org.nyyakko.eryn.Parser;
import org.nyyakko.eryn.Interpreter;

import java.util.ArrayList;
import java.util.Optional;
import java.io.File;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.printf("You must give an eryn source to execute!");
            System.exit(0);
        }

        Optional<ArrayList<String>> maybeSource = Lexer.readSourceContent(new File(args[0]));

        maybeSource
            .map((source) -> {
                Lexer lexer = new Lexer(source);
                return lexer.tokenize();
            })
            .map((tokens) -> {
                Parser parser = new Parser(tokens);
                return parser.parse();
            })
            .ifPresentOrElse((ast) -> {
                Interpreter interpreter = new Interpreter(ast);
                interpreter.interpret();
            },
            () -> System.out.println("Failed to parse source")
            );
    }
}
