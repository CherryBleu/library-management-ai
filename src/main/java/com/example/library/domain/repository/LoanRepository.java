package com.example.library.domain.repository;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import com.example.library.domain.reader.*;
import com.example.library.domain.card.*;
import com.example.library.domain.catalog.*;
import com.example.library.domain.circulation.*;
import com.example.library.domain.reservation.*;
import com.example.library.domain.fine.*;
import com.example.library.domain.admin.Actor;
public interface LoanRepository {
 List<Loan> loans(long readerId); Loan activeLoan(long itemId); long insertLoan(long readerId,long cardId,long itemId,long librarianId,LocalDate today,BorrowPolicy policy,FineRule rule); void closeLoan(long id,LocalDate today,long librarianId); FineRecord fine(long id); FineRecord fineByLoan(long loanId); long insertFine(Loan loan,long days,BigDecimal amount); boolean unpaidFines(long readerId); void adjustFine(long id,BigDecimal amount,String reason,long adminId);
}
