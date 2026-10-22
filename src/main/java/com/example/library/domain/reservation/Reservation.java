package com.example.library.domain.reservation;
import java.time.LocalDate;
import java.time.Instant;
public record Reservation(long id,long readerId,long titleId,long queueNo,String status,Instant createdAt,Long itemId,LocalDate holdUntil) {
 public boolean active(){return "WAITING".equals(status)||"READY".equals(status);}
 public boolean expired(LocalDate today){return "READY".equals(status)&&holdUntil!=null&&today.isAfter(holdUntil);}
}
