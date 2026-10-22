package com.example.library.domain.fine;
import java.math.BigDecimal;
import com.example.library.domain.catalog.ItemType;
import com.example.library.domain.repository.RuleRepository;
import static com.example.library.domain.DomainException.require;
public final class FineRuleService {
 private final RuleRepository rules;
 public FineRuleService(RuleRepository rules){this.rules=rules;}
 public FineRule rule(ItemType type){FineRule r=rules.fineRule(type);require(r!=null,"RULE_NOT_FOUND");return r;}
 public BigDecimal calculateFine(ItemType type,long days){return rule(type).calculate(days);}
 public BigDecimal calculateFine(FineRule snapshot,long days){return snapshot.calculate(days);}
}
