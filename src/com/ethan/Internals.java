package com.ethan;

import java.util.HashMap;

public class Internals {
    public static final String EVAL_SYNTAX = "eval: -?[0-9a-zA-Z]+ (-gt|-lt|-eq|-neq) -?[0-9a-zA-Z]+";
    public static final String EXPR_SYNTAX = "expr: -?[0-9a-zA-Z]+ (-gt|-lt|-eq|-neq) -?[0-9a-zA-Z]+";
    public static final String OP_SYNTAX = "-?[0-9]+ -?[0-9]+";
    public static HashMap<String, Boolean> bVar = new HashMap<>();
    public static HashMap<String, Integer> var = new HashMap<>();

    public static int add = 0;
    public static int sub = 0;
    public static int mult = 0;
    public static int div = 0;
    public static Boolean expr = false;
}
