package com.example.library.domain.circulation;
import com.example.library.domain.reader.ReaderType;
public record BorrowPolicy(long id,ReaderType readerType,int maxBorrowCount,int borrowDays) {
 public BorrowPolicy { if(maxBorrowCount<=0||borrowDays<=0)throw new IllegalArgumentException("Positive policy values required"); }
 public boolean allows(int currentCount){return currentCount<maxBorrowCount;}
}
