package com.example.library.domain.fine;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.example.library.domain.catalog.ItemType;
public record FineRule(long id,ItemType itemType,BigDecimal dailyRate,BigDecimal maximumAmount) {
 public FineRule { if(dailyRate.signum()<0||maximumAmount.signum()<0)throw new IllegalArgumentException("Negative rate"); }
 public BigDecimal calculate(long days){return dailyRate.multiply(BigDecimal.valueOf(Math.max(0,days))).min(maximumAmount).setScale(2,RoundingMode.HALF_UP);}
}
