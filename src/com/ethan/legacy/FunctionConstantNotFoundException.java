package com.ethan.legacy;

public class FunctionConstantNotFoundException extends Exception{
    public FunctionConstantNotFoundException() {}

    // Constructor that accepts a message
    public FunctionConstantNotFoundException(String message)
    {
        super(message);
    }
}
