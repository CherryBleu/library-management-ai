package com.example.library.domain.fine;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import com.example.library.domain.circulation.Loan;
import com.example.library.domain.catalog.ItemType;
public final class FineService {
 public long overdueDays(Loan loan,LocalDate returned){return Math.max(0,ChronoUnit.DAYS.between(loan.dueAt(),returned));}
 public BigDecimal calculate(Loan loan,LocalDate returned){return new FineRule(loan.fineRuleId(),ItemType.THESIS,loan.dailyRate(),loan.maximumAmount()).calculate(overdueDays(loan,returned));}
}
