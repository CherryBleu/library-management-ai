package com.example.library.domain.circulation;
import java.time.LocalDate;
import java.util.List;
import com.example.library.domain.reader.*;
import com.example.library.domain.repository.RuleRepository;
import static com.example.library.domain.DomainException.require;
public final class BorrowPolicyService {
 private final RuleRepository rules;
 public BorrowPolicyService(RuleRepository rules){this.rules=rules;}
 public BorrowPolicy policy(ReaderType type){BorrowPolicy p=rules.policy(type);require(p!=null,"RULE_NOT_FOUND");return p;}
 public int getMaxBorrowCount(ReaderType type){return policy(type).maxBorrowCount();}
 public int getBorrowDays(ReaderType type){return policy(type).borrowDays();}
 public boolean canBorrow(Reader reader,List<Loan> loans,LocalDate today){
 require(reader.active(),"INVALID_CARD");
 require(policy(reader.readerType()).allows((int)loans.stream().filter(Loan::active).count()),"BORROW_LIMIT");
 require(loans.stream().noneMatch(l->l.isOverdue(today)),"OVERDUE_LOAN");
 return true;
 }
}
