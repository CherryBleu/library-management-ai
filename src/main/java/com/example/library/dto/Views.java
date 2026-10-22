package com.example.library.dto;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import com.example.library.domain.reader.*;
import com.example.library.domain.card.*;
import com.example.library.domain.catalog.*;
import com.example.library.domain.circulation.*;
import com.example.library.domain.fine.*;
import com.example.library.domain.reservation.*;
public final class Views {
 public record ReaderView(long id,String name,ReaderType readerType,String status){}
 public static ReaderView reader(Reader r){return new ReaderView(r.id(),r.name(),r.readerType(),r.status());}
 public record CardView(long id,String cardNo,long readerId,LocalDate expiresAt,String status){}
 public static CardView card(BorrowCard c){return new CardView(c.id(),c.cardNo(),c.readerId(),c.expiresAt(),c.status());}
 public record ItemView(long id,long titleId,String barcode,String location,String status,boolean borrowable){}
 public static ItemView item(LibraryItem i){return new ItemView(i.id(),i.titleId(),i.barcode(),i.location(),i.status(),i.borrowable());}
 public record TitleView(long id,String title,String authors,String standardNo,ItemType itemType,String status,List<ItemView> copies){}
 public static TitleView title(BookTitle t,List<LibraryItem> items){return new TitleView(t.id(),t.title(),t.authors(),t.standardNo(),t.itemType(),t.status(),items.stream().map(Views::item).toList());}
 public record LoanView(long id,long readerId,long itemId,LocalDate borrowedAt,LocalDate dueAt,LocalDate returnedAt,String status){}
 public static LoanView loan(Loan l,LocalDate today){return new LoanView(l.id(),l.readerId(),l.itemId(),l.borrowedAt(),l.dueAt(),l.returnedAt(),l.status(today));}
 public record ReturnView(LoanView loan,FineRecord fine){}
 public record ReservationView(long id,long readerId,long titleId,long queueNo,String status,LocalDate holdUntil,Long itemId){}
 public static ReservationView reservation(Reservation r){return new ReservationView(r.id(),r.readerId(),r.titleId(),r.queueNo(),r.status(),r.holdUntil(),r.itemId());}
}
