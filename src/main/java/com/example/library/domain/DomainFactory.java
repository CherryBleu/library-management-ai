package com.example.library.domain;
import com.example.library.domain.reader.*;
import com.example.library.domain.catalog.*;
public final class DomainFactory {
 private DomainFactory(){}
 public static Reader reader(long id,String name,String dep,ReaderType type,String status){
 return type==ReaderType.TEACHER?new TeacherReader(id,name,dep,type,status):new StudentReader(id,name,dep,type,status);}
 public static LibraryItem item(long id,long title,String barcode,String location,String status,boolean borrowable,ItemType type){
 return switch(type.category()){
 case "BOOK" -> new Book(id,title,barcode,location,status,borrowable,type);
 case "MAGAZINE" -> new Magazine(id,title,barcode,location,status,borrowable,type);
 default -> new Thesis(id,title,barcode,location,status,borrowable,type);};}
}
