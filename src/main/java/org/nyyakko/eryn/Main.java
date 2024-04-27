package org.nyyakko.eryn;

import org.nyyakko.eryn.Lexer;
import org.nyyakko.eryn.Parser;
import org.nyyakko.eryn.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

public class Main
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

    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.printf("You must give an eryn source to execute!");
            System.exit(0);
        }

        Optional<ArrayList<String>> maybeSource = readSourceContent(new File(args[0]));

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
