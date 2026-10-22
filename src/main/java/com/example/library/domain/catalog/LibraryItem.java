package com.example.library.domain.catalog;
public abstract class LibraryItem {
 private final long id,titleId; private final String barcode,location,status; private final boolean borrowable; private final ItemType itemType;
 protected LibraryItem(long id,long titleId,String barcode,String location,String status,boolean borrowable,ItemType type){
 this.id=id;this.titleId=titleId;this.barcode=barcode;this.location=location;this.status=status;this.borrowable=borrowable;this.itemType=type;}
 public long id(){return id;} public long titleId(){return titleId;} public String barcode(){return barcode;}
 public String location(){return location;} public String status(){return status;} public boolean borrowable(){return borrowable;}
 public ItemType itemType(){return itemType;} public boolean available(){return borrowable && "AVAILABLE".equals(status);}
}
