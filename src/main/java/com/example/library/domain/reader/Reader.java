package com.example.library.domain.reader;
public abstract class Reader {
 private final long id; private final String name,department,status; private final ReaderType readerType;
 protected Reader(long id,String name,String department,ReaderType type,String status){this.id=id;this.name=name;this.department=department;this.readerType=type;this.status=status;}
 public long id(){return id;} public String name(){return name;} public String department(){return department;}
 public ReaderType readerType(){return readerType;} public String status(){return status;}
 public boolean active(){return "ACTIVE".equals(status);}
}
