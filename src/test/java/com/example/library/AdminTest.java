package com.example.library;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.library.application.*;
import com.example.library.dto.Requests;
import com.example.library.domain.admin.Actor;
import com.example.library.domain.reader.ReaderType;
import com.example.library.domain.catalog.ItemType;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
class AdminTest extends IntegrationSupport {
 @Autowired AdminService administration; @Autowired CirculationService circulation; @Autowired QueryService query;
 @Test void TC028_lastAdmin(){assertThatThrownBy(()->administration.removeStaff(admin,true,1)).hasMessage("LAST_ADMIN");long second=administration.addStaff(admin,true,new Requests.AddStaff("Second","A2"));administration.removeStaff(admin,true,second);assertThat(repo.activeAdmins()).isEqualTo(1);}
 @Test void librarianLifecycle(){long id=administration.addStaff(admin,false,new Requests.AddStaff("L","L2"));assertThat(repo.actorActive(new Actor(Actor.Role.LIBRARIAN,id))).isTrue();administration.removeStaff(admin,false,id);assertThatThrownBy(()->query.books(new Actor(Actor.Role.LIBRARIAN,id),"")).hasMessage("FORBIDDEN");}
 @Test void TC025_rulesVersion(){long old=repo.policy(ReaderType.UNDERGRADUATE).id();administration.policy(admin,ReaderType.UNDERGRADUATE,new Requests.UpdatePolicy(6,40));assertThat(repo.policy(ReaderType.UNDERGRADUATE).id()).isNotEqualTo(old);assertThat(repo.policy(ReaderType.UNDERGRADUATE).borrowDays()).isEqualTo(40);administration.fineRule(admin,ItemType.THESIS,new Requests.UpdateFineRule(BigDecimal.ONE,BigDecimal.TEN));assertThat(repo.fineRule(ItemType.THESIS).dailyRate()).isEqualByComparingTo("1");}
 @Test void TC026_invalidRules(){assertThatThrownBy(()->administration.policy(admin,ReaderType.TEACHER,new Requests.UpdatePolicy(0,1))).hasMessage("INVALID_INPUT");assertThat(repo.policy(ReaderType.TEACHER).maxBorrowCount()).isEqualTo(20);}
 @Test void TC029_fineAdjustment(){long r=reader("S");var c=card(r);item(title("T"),"B");circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"));db.update("UPDATE loans SET borrowed_at=?,due_at=?",today.minusDays(32),today.minusDays(2));var result=circulation.returnBook(librarian,new Requests.ReturnBook("B"));long id=result.fine().id();
 assertThatThrownBy(()->administration.adjustFine(admin,id,new Requests.AdjustFine(BigDecimal.ZERO,""))).hasMessage("INVALID_INPUT");
 administration.adjustFine(admin,id,new Requests.AdjustFine(BigDecimal.ZERO,"Correction"));assertThat(repo.fine(id).status()).isEqualTo("WAIVED");
 assertThat(administration.audit(admin)).anyMatch(x->"ADJUST_FINE".equals(x.get("ACTION")));
 }
}
