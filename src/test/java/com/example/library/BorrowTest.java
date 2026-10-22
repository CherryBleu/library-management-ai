package com.example.library;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.library.application.CirculationService;
import com.example.library.dto.Requests;
import static org.assertj.core.api.Assertions.*;
class BorrowTest extends IntegrationSupport {
 @Autowired CirculationService circulation;
 @Test void TC004_success(){long r=reader("S1");var c=card(r);long t=title("T1");long i=item(t,"B1");
 var loan=circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B1"));
 assertThat(loan.dueAt()).isEqualTo(today.plusDays(30));assertThat(repo.item(i).status()).isEqualTo("ON_LOAN");assertThat(repo.loans(r)).hasSize(1);
 }
 @Test void TC003_invalidCards(){long r=reader("S1");var c=card(r);long t=title("T");item(t,"B");
 assertThatThrownBy(()->circulation.borrow(librarian,new Requests.BorrowBook("MISSING","B"))).hasMessage("INVALID_CARD");
 db.update("UPDATE borrow_cards SET issued_at=?,expires_at=? WHERE card_id=?",today.minusYears(1),today.minusDays(1),c.id());
 assertThatThrownBy(()->circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"))).hasMessage("INVALID_CARD");
 }
 @Test void TC005_limit(){long r=reader("S");var c=card(r);long t=title("T");
 for(int n=0;n<5;n++){item(t,"B"+n);circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"+n));}
 long last=item(t,"B5");assertThatThrownBy(()->circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B5"))).hasMessage("BORROW_LIMIT");assertThat(repo.item(last).status()).isEqualTo("AVAILABLE");
 }
 @Test void TC006_overdue(){long r=reader("S");var c=card(r);long t=title("T");item(t,"B");item(t,"C");
 circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"));
 db.update("UPDATE loans SET borrowed_at=?,due_at=?",today.minusDays(40),today.minusDays(1));
 assertThatThrownBy(()->circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"C"))).hasMessage("OVERDUE_LOAN");
 }
 @Test void TC007_unavailable(){long r=reader("S");var c=card(r);long t=title("T");long i=item(t,"B");
 circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"));
 assertThatThrownBy(()->circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"))).hasMessage("ITEM_UNAVAILABLE");
 assertThat(repo.loans(r)).hasSize(1);
 }
}
