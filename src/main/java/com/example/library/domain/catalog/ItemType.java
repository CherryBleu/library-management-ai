package com.example.library.domain.catalog;
public enum ItemType { CHINESE_BOOK, FOREIGN_BOOK, CHINESE_MAGAZINE, FOREIGN_MAGAZINE, THESIS;
 public String language(){return name().startsWith("FOREIGN")?"FOREIGN":"ZH";}
 public String category(){return this==THESIS?"THESIS":name().contains("MAGAZINE")?"MAGAZINE":"BOOK";}
}
