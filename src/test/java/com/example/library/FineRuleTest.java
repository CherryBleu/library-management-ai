package com.example.library;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import com.example.library.domain.fine.*;
import com.example.library.domain.catalog.ItemType;
import com.example.library.domain.repository.RuleRepository;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class FineRuleTest {
 @ParameterizedTest @CsvSource({"CHINESE_BOOK,0.50,100","FOREIGN_BOOK,1.00,200","CHINESE_MAGAZINE,1.00,100","FOREIGN_MAGAZINE,2.00,200","THESIS,2.00,300"})
 void zeroDailyAndCap(ItemType type,BigDecimal rate,BigDecimal cap){
 RuleRepository repo=mock(RuleRepository.class);when(repo.fineRule(type)).thenReturn(new FineRule(1,type,rate,cap));
 var s=new FineRuleService(repo);
 assertThat(s.calculateFine(type,-1)).isEqualByComparingTo("0");
 assertThat(s.calculateFine(type,0)).isEqualByComparingTo("0");
 assertThat(s.calculateFine(type,2)).isEqualByComparingTo(rate.multiply(BigDecimal.valueOf(2)));
 assertThat(s.calculateFine(type,10000)).isEqualByComparingTo(cap);
 }
}
