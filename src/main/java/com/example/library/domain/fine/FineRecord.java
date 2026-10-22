package com.example.library.domain.fine;
import java.math.BigDecimal;
public record FineRecord(long id,long loanId,long overdueDays,BigDecimal amount,String status) {}
