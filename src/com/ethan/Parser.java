package com.ethan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import static com.ethan.Functions.*;

public class Parser {
    public Parser(File mainFile) {
        assert Files.exists( mainFile.toPath() );

        try {
            BufferedReader r = new BufferedReader( new FileReader(mainFile) );
            Line l = new Line();

            while( ( l.set( r.readLine() ) ) != null ) {
                if( l.str.isBlank() ) continue;

                if( !l.str.matches( "(^:end$)|^[a-zA-Z]+: ( ?(.+))$" ) ) {
                    System.err.println("Syntax error at: " + mainFile + ':' + l.getLine() + "'" + l.str + "'");
                    System.exit(1);
                }

                String fn = l.str.substring( 0, l.str.indexOf(' ') );
                String[] args = l.str.substring( l.str.indexOf(' ') ).trim().split(" ");

                switch(fn) {
                    case "setv:" -> Executor.handle( FN_SETV, args );
                    case "out:" -> Executor.handle( FN_OUT, args );

                    case "eval:" -> {
                        int line = Executor.handle( FN_EVAL, args, mainFile, l.getLine() );
                        int x = l.getLine();
                        while(x != line + 1) {
                            l.set( r.readLine() );
                            x++;
                        }
                    }

                    case "expr:" -> Executor.handle( FN_EXPR, args, l.str );
                    case "add:" -> Executor.handle( FN_ADD, args );
                    case "div:" -> Executor.handle( FN_DIV, args );
                    case "sub:" -> Executor.handle( FN_SUB, args );
                    case "mult:" -> Executor.handle( FN_MULT, args );
                }
            }
            r.close();
        } catch(IOException ignored) {

        }
    }

    public static void parseLn(String ln) {
        String function = ln.substring( 0, ln.indexOf(' ') );
        String[] args = ln.substring( ln.indexOf(' ') ).trim().split(" ");
        switch(function) {
            case "setv:" -> Executor.handle(FN_SETV, args);
            case "out:" -> Executor.handle(FN_OUT, args);
        }
    }
}
