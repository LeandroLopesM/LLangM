package com.ethan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class Executor {
    /*
    *           Main function execution handler
    */
    public static int handle( Functions fn, String[] args, Object... opts ) {
        switch(fn) {
            case FN_SETV -> setv(args);
            case FN_OUT -> out(args);

            case FN_EVAL -> {
                assert opts != null
                    :"ERROR Executor:handle():EVAL -> Expected [Functions ID, String[] args, Object... (fileName, fileLine)]\n" +
                    "Received: [---, Object... (null)]";

                assert opts[0] instanceof String && opts[1] instanceof Integer
                    :"ERROR Executor:handle():EVAL -> Expected [Functions ID, String[] args, Object... (fileName, fileLine)]\n" +
                    "Received: [---, Object...(" + opts[0].getClass() + ", " + opts[1].getClass() + ')' +
                ']';

                return eval( args, opts[0], opts[1] );
            }

            case FN_EXPR -> {
                assert opts[0] instanceof String
                    :"ERROR Executor:handle():EXPR -> Expected [Functions ID, String[] args, Object... (currentLine)\n" +
                    "Received: [---, "
                    + ( (opts[0] == null)? "null"
                    : ("Object... (" + opts[0].getClass() + ')') )
                + "]";
                expr( args, (String) opts[0] );
            }
            case FN_ADD -> add(args);
            case FN_SUB -> sub(args);
            case FN_MULT -> mult(args);
            case FN_DIV -> div(args);
        }
        return 0;
    }

    /*                     OPERATION METHODS
     *              Functions used in place of operations
     *  Output goes to a variable in Internals.java labeled #<functionName>
     */

    private static void add(String[] args) {
        assert String.join(" ", args).matches( Internals.OP_SYNTAX ) : "SYNTAX ERROR -> Add: function parameters are not integers or (int)variables";
        Internals.add = ( Integer.parseInt( args[0] ) + Integer.parseInt( args[1] ) );
    }
    private static void sub(String[] args) {
        assert String.join( " ", args ).matches( Internals.OP_SYNTAX ) : "SYNTAX ERROR -> Sub: function parameters are not integers or (int)variables";
        Internals.sub = ( Integer.parseInt( args[0] ) - Integer.parseInt( args[1] ) );
    }
    private static void mult(String[] args) {
        assert String.join(" ", args).matches(Internals.OP_SYNTAX) : "SYNTAX ERROR -> Mult: function parameters are not integers or (int)variables";
        Internals.mult = ( Integer.parseInt( args[0] ) * Integer.parseInt( args[1] ) );
    }
    private static void div(String[] args) {
        assert String.join(" ", args).matches(Internals.OP_SYNTAX) : "SYNTAX ERROR -> Div: function parameters are not integers or (int)variables";
        Internals.div = ( Integer.parseInt( args[0] ) / Integer.parseInt( args[1] ) );
    }

    /*
    *        Expression handler (Haven't tested)
    */

    private static void expr(String[] args, String line) {
        assert line.matches( Internals.EXPR_SYNTAX );

        Integer lhs = 0;
        Integer rhs = 0;

        boolean bls = false;
        boolean brs = false;
        

//        if(args[0].matches("(@?[a-zA-Z]#?)+") || args[2].matches("([a-zA-Z]#?)+")) {
        if(args[0].matches("[-0-9]+")) lhs = Integer.parseInt(args[0]);
        else if(args[0].contains("#")) {
            switch(args[0]) {
                case "#add" -> lhs = Internals.add;
                case "#mult" -> lhs = Internals.mult;
                case "#sub" -> lhs = Internals.sub;
                case "#div" -> lhs = Internals.div;
                case "#expr" -> bls = Internals.expr;
            }
        }
        else if(Internals.var.containsKey(args[0])) {
            lhs = Internals.var.get(args[0]);
        }
        else if(args[0].contains("@") && Internals.bVar.containsKey(args[0])) {
            bls = Internals.bVar.get(args[0]);
            lhs = null;
        }

        if(args[2].matches("[-0-9]+")) rhs = Integer.parseInt(args[2]);
        else if(args[2].contains("#")) {
            switch(args[2]) {
                case "#add" -> rhs = Internals.add;
                case "#mult" -> rhs = Internals.mult;
                case "#sub" -> rhs = Internals.sub;
                case "#div" -> rhs = Internals.div;
                case "#expr" -> brs = Internals.expr;
            }
        }
        else if(Internals.var.containsKey(args[2])) {
            rhs = Internals.var.get(args[2]);
        }
        else if(args[2].contains("@") && Internals.bVar.containsKey(args[0])) {
            brs = Internals.bVar.get(args[2]);
            rhs = null;
        }

        if(rhs == null && lhs == null) {
            switch(args[1]) {
                case "-eq" -> Internals.expr = bls == brs;
                case "-neq" -> Internals.expr = bls != brs;
                default -> {
                    assert true : "Unkown operator";
                }
            }
            return;
        }
        if(rhs != null && lhs != null) {
            switch (args[1]) {
                case "-gt" -> Internals.expr =   lhs > rhs;
                case "-lt" -> Internals.expr =   lhs < rhs;
                case "-eq" -> Internals.expr =   lhs.equals(rhs);
                case "-neq" -> Internals.expr = !lhs.equals(rhs);
                default -> {
                    assert true : "Unknown operator";
                }
            }
        }
        else assert true : "Mixing between boolean and int";
    }

    /*
    *       Primary output function
    */

    private static void out(String[] args) {
        String fmt = String.join( " ", args );

        if( fmt.matches( "(.*)(\\{[#@]?[a-zA-Z]+})+(.*)" ) ) {
            String key = fmt.substring( fmt.indexOf( '{' ) + 1, fmt.indexOf( '}' ) );
            String val = null;

            if(key.contains("@") && Internals.bVar.containsKey(key)) {
                val = Internals.bVar.get(key).toString();
            } else if(key.contains("#")) {
                switch(key) {
                    case "#add" -> val = String.valueOf(Internals.add);
                    case "#mult" -> val = String.valueOf(Internals.mult);
                    case "#sub" -> val = String.valueOf(Internals.sub);
                    case "#div" -> val = String.valueOf(Internals.div);
                    case "#expr" -> val = Internals.expr.toString();
                }
            } else if(Internals.var.containsKey(key)) {
                val = String.valueOf(Internals.var.get(key));
            }

            assert val != null;
            fmt = fmt.replace( ( "{" + key + "}" ), val );
        }

        System.out.println( fmt.replace( "\"", "" ) );
    }

    /*
    *       Variable manipulation method
    */

    private static void setv(String[] args) {
        String label = args[0];

        if( label.startsWith( "@" ) ) {
            Boolean val = null;

            if( args[1].startsWith( "@" ) && Internals.bVar.containsKey( args[1] ) ) {
                val = Internals.bVar.get( args[1] );
            }
            else if( args[1].matches( "(true|false)$" ) ) {
                val = Boolean.parseBoolean( args[1] );
            }
            else {
                assert true : "Syntax ERROR -> SetV: attempt to set boolean variable content to unknown type";
            }

            Internals.bVar.put( label, val );
            return;
        }

        if( args[1].matches( "^(#add|#div|#mult|#sub)$") ) {
            int val = 0;

            switch( args[1] ) {
                case "#add" -> val = Internals.add;
                case "#div" -> val = Internals.div;
                case "#mult" -> val = Internals.mult;
                case "#sub" -> val = Internals.sub;
            }

            if( Internals.var.containsKey(args[0]) ) {
                Internals.var.replace( args[0], val );
                return;
            }
            Internals.var.put( args[0], val );
            return;
        }

        int value = ( args[1].matches( "-?[0-9]+" ) )? Integer.parseInt( args[1] ) : 0;

        if( Internals.var.containsKey(args[0]) ) {
            Internals.var.replace( args[0], value );
            return;
        }
        else if(Internals.var.containsKey(args[1])) {
            value = Internals.var.get( args[1] );
        }

        Internals.var.put( args[0], value );
    }

    /*
    *       Evaluation method/block
    */

    private static int eval(String[] args, Object fileName, Object line) {
        Integer l = (Integer) line;
        String b = " ";

        try {
            BufferedReader r = new BufferedReader( new FileReader( (File) fileName ) );
            int i = 0;

            while (i < l) {
                b = r.readLine();
                i++;
            }

            assert b != null && b.matches( Internals.EVAL_SYNTAX );

            Integer lhs = null;
            Integer rhs = null;

            if( args[0].matches( "[0-9]+" ) ) lhs = Integer.parseInt(args[0]);
            else if( args[0].matches( "[a-zA-Z]+" ) ) {
                assert Internals.var.containsKey( args[0] );

                lhs = Internals.var.get( args[0] );
            }

            if( args[2].matches( "[0-9]+" ) ) rhs = Integer.parseInt( args[2] );
            else if( args[2].matches( "[a-zA-Z]" ) ) {
                assert Internals.var.containsKey( args[2] );

                rhs = Internals.var.get( args[2] );
            }

            boolean isTrue = false;
            assert lhs != null;
            assert rhs != null;

            switch(args[1]) {
                case "-gt" -> isTrue = lhs > rhs;
                case "-lt" -> isTrue = lhs < rhs;
                case "-eq" -> isTrue = Objects.equals( lhs, rhs );
                case "-neq" -> isTrue = !Objects.equals( lhs, rhs );
                default -> {
                    assert true : "Unknown operator";
                }
            }

            while(b != null) {
                b = r.readLine();
                assert b != null;
                if(b.isEmpty()) continue;

                if(b.equals(":end")) break;

                i++;
                if(isTrue) Parser.parseLn(b.trim());
            }
            return i;

        } catch(IOException e) {
            throw new RuntimeException();
        }
    }




}
