package com.udemy.libraryapi.exception;

public class BusinessException extends RuntimeException{
    public BusinessException(String msg){
        super(msg);
    }
}
