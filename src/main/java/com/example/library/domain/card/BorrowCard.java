package com.example.library.domain.card;
import java.time.LocalDate;
public record BorrowCard(long id,String cardNo,long readerId,LocalDate expiresAt,String status) {
 public boolean valid(LocalDate today){return "ACTIVE".equals(status) && !expiresAt.isBefore(today);}
}
