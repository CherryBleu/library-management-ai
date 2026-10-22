package com.example.library.domain.catalog;
public final class Book extends LibraryItem {
 public Book(long id,long titleId,String barcode,String location,String status,boolean borrowable,ItemType type){
 super(id,titleId,barcode,location,status,borrowable,type);}
}
