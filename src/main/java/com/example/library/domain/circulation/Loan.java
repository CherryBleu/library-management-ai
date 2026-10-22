package com.example.library.domain.circulation;
import java.time.LocalDate;
import java.math.BigDecimal;
public record Loan(long id,long readerId,long itemId,long cardId,LocalDate borrowedAt,LocalDate dueAt,
 LocalDate returnedAt,BigDecimal dailyRate,BigDecimal maximumAmount,long fineRuleId) {
 public boolean active(){return returnedAt==null;}
 public boolean isOverdue(LocalDate today){return active() && dueAt.isBefore(today);}
 public String status(LocalDate today){return !active()?"RETURNED":isOverdue(today)?"OVERDUE":"BORROWED";}
}
