package com.example.library;
import org.junit.jupiter.api.Test;
import com.example.library.dto.Requests;
import com.example.library.domain.admin.Actor;
import static org.assertj.core.api.Assertions.*;
class CardTest extends IntegrationSupport {
 @Test void TC001_TC002_registerAndIssue(){
 long id=reader("S1");var c=card(id);assertThat(c.cardNo()).isEqualTo("CARD2026000001");
 assertThat(repo.loans(id)).isEmpty();assertThat(repo.reader(id).name()).isEqualTo("S1");
 }
 @Test void TC015_missingReader(){assertThatThrownBy(()->card(999)).hasMessage("READER_NOT_FOUND");}
 @Test void TC017_duplicateCard(){long id=reader("S1");card(id);assertThatThrownBy(()->card(id)).hasMessage("DUPLICATE_CARD");}
 @Test void TC014_readerCannotIssue(){
 long id=reader("S1");
 assertThatThrownBy(()->cards.issue(new Actor(Actor.Role.READER,id),new Requests.IssueCard(id,today.plusYears(1)))).hasMessage("FORBIDDEN");
 assertThat(repo.activeCard(id)).isNull();assertThat(repo.audits()).anyMatch(x->"FORBIDDEN".equals(x.get("RESULT")));
 }
 @Test void invalidExpiry(){long id=reader("S1");assertThatThrownBy(()->cards.issue(admin,new Requests.IssueCard(id,today.minusDays(1)))).hasMessage("INVALID_INPUT");}
}

