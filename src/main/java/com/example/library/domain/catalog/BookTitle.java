package com.example.library.domain.catalog;
public record BookTitle(long id,String title,String authors,String standardNo,ItemType itemType,String status) {
 public boolean active(){return "ACTIVE".equals(status);}
}
