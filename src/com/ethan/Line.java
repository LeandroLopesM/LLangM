package com.ethan;

public class Line {
    private int line = 0;
    public String str = " ";

    String set(String s) {
        str = s;
        line++;

        return s;
    }

    int getLine() {
        return line;
    }
}
