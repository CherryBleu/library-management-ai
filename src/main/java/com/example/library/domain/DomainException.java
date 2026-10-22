package com.example.library.domain;
public class DomainException extends RuntimeException {
 private final String code;
 public DomainException(String code) { super(code); this.code=code; }
 public String code(){return code;}
 public static void require(boolean condition,String code){if(!condition) throw new DomainException(code);}
}
