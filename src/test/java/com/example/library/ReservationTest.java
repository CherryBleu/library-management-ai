package com.example.library;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.library.application.*;
import com.example.library.dto.Requests;
import com.example.library.domain.admin.Actor;
import static org.assertj.core.api.Assertions.*;
class ReservationTest extends IntegrationSupport {
 @Autowired ReservationService reservations; @Autowired CirculationService circulation;
 private long unavailable(){long t=title("T");long i=item(t,"B");db.update("UPDATE library_items SET status='ON_LOAN' WHERE item_id=?",i);return t;}
 private Actor actor(String no){long id=reader(no);card(id);return new Actor(Actor.Role.READER,id);}
 @Test void TC012_TC013_queueAndDuplicate(){long t=unavailable();var a=actor("S1");var b=actor("S2");var first=reservations.reserve(a,new Requests.ReserveBook(t));var second=reservations.reserve(b,new Requests.ReserveBook(t));assertThat(first.queueNo()).isLessThan(second.queueNo());assertThatThrownBy(()->reservations.reserve(a,new Requests.ReserveBook(t))).hasMessage("DUPLICATE_RESERVATION");}
 @Test void TC015_missingReader(){assertThatThrownBy(()->reservations.reserve(new Actor(Actor.Role.READER,999),new Requests.ReserveBook(1))).hasMessage("READER_NOT_FOUND");}
 @Test void TC016_missingTitle(){var a=actor("S");assertThatThrownBy(()->reservations.reserve(a,new Requests.ReserveBook(999))).hasMessage("TITLE_NOT_FOUND");}
 @Test void TC033_available(){long t=title("T");item(t,"B");var a=actor("S");assertThatThrownBy(()->reservations.reserve(a,new Requests.ReserveBook(t))).hasMessage("ITEM_AVAILABLE");}
 @Test void TC022_queueCollection(){
 long borrower=reader("BORROWER");var c=card(borrower);long t=title("T");item(t,"B");circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"));
 var a=actor("S1");var b=actor("S2");var first=reservations.reserve(a,new Requests.ReserveBook(t));reservations.reserve(b,new Requests.ReserveBook(t));
 circulation.returnBook(librarian,new Requests.ReturnBook("B"));assertThat(repo.reservation(first.id()).status()).isEqualTo("READY");
 assertThatThrownBy(()->circulation.borrow(librarian,new Requests.BorrowBook(repo.activeCard(b.id()).cardNo(),"B"))).hasMessage("ITEM_UNAVAILABLE");
 circulation.borrow(librarian,new Requests.BorrowBook(repo.activeCard(a.id()).cardNo(),"B"));assertThat(repo.reservation(first.id()).status()).isEqualTo("FULFILLED");
 }
 @Test void TC023_timeoutAndCancel(){
 long t=unavailable();var a=actor("A");var b=actor("B");var first=reservations.reserve(a,new Requests.ReserveBook(t));var second=reservations.reserve(b,new Requests.ReserveBook(t));
 db.update("UPDATE library_items SET status='AVAILABLE'");reservations.expire(admin);
 db.update("UPDATE reservations SET hold_until=? WHERE reservation_id=?",today.minusDays(1),first.id());reservations.expire(admin);
 assertThat(repo.reservation(first.id()).status()).isEqualTo("EXPIRED");assertThat(repo.reservation(second.id()).status()).isEqualTo("READY");
 reservations.cancel(b,second.id());assertThat(repo.item("B").status()).isEqualTo("AVAILABLE");
 }
 @Test void TC024_limit(){
 var a=actor("A");for(int n=0;n<5;n++){long t=title("T"+n);item(t,"B"+n);db.update("UPDATE library_items SET status='ON_LOAN'");reservations.reserve(a,new Requests.ReserveBook(t));}
 long t=title("SIX");item(t,"SIX");db.update("UPDATE library_items SET status='ON_LOAN'");
 assertThatThrownBy(()->reservations.reserve(a,new Requests.ReserveBook(t))).hasMessage("RESERVATION_LIMIT");
 }
}
