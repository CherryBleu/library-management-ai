package com.example.library;
import com.example.library.domain.circulation.*;
import com.example.library.domain.reader.*;
import com.example.library.domain.repository.RuleRepository;
import com.example.library.domain.DomainFactory;
import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class BorrowPolicyTest {
 @ParameterizedTest @CsvSource({"UNDERGRADUATE,5,30","GRADUATE,10,60","DOCTORAL,15,90","JUNIOR_COLLEGE,5,30","TEACHER,20,90"})
 void limits(ReaderType type,int count,int days){
 RuleRepository rules=mock(RuleRepository.class);when(rules.policy(type)).thenReturn(new BorrowPolicy(1,type,count,days));
 BorrowPolicyService s=new BorrowPolicyService(rules);
 assertThat(s.getMaxBorrowCount(type)).isEqualTo(count);assertThat(s.getBorrowDays(type)).isEqualTo(days);
 var reader=DomainFactory.reader(1,"A","CS",type,"ACTIVE");LocalDate d=LocalDate.of(2026,10,22);
 Loan loan=new Loan(1,1,1,1,d,d.plusDays(1),null,BigDecimal.ONE,BigDecimal.TEN,1);
 assertThat(s.canBorrow(reader,Collections.nCopies(count-1,loan),d)).isTrue();
 assertThatThrownBy(()->s.canBorrow(reader,Collections.nCopies(count,loan),d)).hasMessage("BORROW_LIMIT");
 }
 @Test void overdueBlocks(){
 RuleRepository rules=mock(RuleRepository.class);when(rules.policy(any())).thenReturn(new BorrowPolicy(1,ReaderType.TEACHER,20,90));
 var s=new BorrowPolicyService(rules);LocalDate d=LocalDate.of(2026,10,22);
 Loan loan=new Loan(1,1,1,1,d.minusDays(10),d.minusDays(1),null,BigDecimal.ONE,BigDecimal.TEN,1);
 assertThatThrownBy(()->s.canBorrow(DomainFactory.reader(1,"A","CS",ReaderType.TEACHER,"ACTIVE"),List.of(loan),d)).hasMessage("OVERDUE_LOAN");
 }
}
