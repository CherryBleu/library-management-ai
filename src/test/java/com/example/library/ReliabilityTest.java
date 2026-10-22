package com.example.library;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.library.application.*;
import com.example.library.domain.DomainException;
import com.example.library.domain.admin.Actor;
import com.example.library.dto.Requests;
import java.util.concurrent.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
class ReliabilityTest extends IntegrationSupport {
 @Autowired CirculationService circulation; @Autowired ReservationService reservations; @Autowired CatalogService catalog;
 @Test void TC030_concurrentBorrow() throws Exception {
 long a=reader("A"),b=reader("B");var ca=card(a);var cb=card(b);item(title("T"),"B");
 var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);
 try{
 List<Future<String>> jobs=new ArrayList<>();
 for(var c:List.of(ca,cb))jobs.add(pool.submit(()->{start.await();try{circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"));return "OK";}catch(DomainException e){return e.code();}}));
 start.countDown();List<String> results=new ArrayList<>();for(var j:jobs)results.add(j.get(15,TimeUnit.SECONDS));
 assertThat(results).containsExactlyInAnyOrder("OK","ITEM_UNAVAILABLE");assertThat(db.queryForObject("SELECT COUNT(*) FROM loans WHERE returned_at IS NULL",Integer.class)).isEqualTo(1);
 }finally{pool.shutdownNow();}
 }
 @Test void TC031_borrowRollback(){
 long r=reader("S");var c=card(r);item(title("T"),"B");
 db.execute("ALTER TABLE audit_logs ADD CONSTRAINT reject_borrow CHECK(action<>'BORROW')");
 assertThatThrownBy(()->circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"))).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
 assertThat(repo.loans(r)).isEmpty();assertThat(repo.item("B").status()).isEqualTo("AVAILABLE");
 }
 @Test void returnRollbackIncludingFineAndQueue(){
 long r=reader("S");var c=card(r);long t=title("T");item(t,"B");circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"));
 long other=reader("OTHER");card(other);var q=reservations.reserve(new Actor(Actor.Role.READER,other),new Requests.ReserveBook(t));
 db.execute("ALTER TABLE audit_logs ADD CONSTRAINT reject_return CHECK(action<>'RETURN')");
 assertThatThrownBy(()->circulation.returnBook(librarian,new Requests.ReturnBook("B"))).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
 assertThat(repo.activeLoan(repo.item("B").id())).isNotNull();assertThat(repo.item("B").status()).isEqualTo("ON_LOAN");
 assertThat(repo.reservation(q.id()).status()).isEqualTo("WAITING");assertThat(db.queryForObject("SELECT COUNT(*) FROM fine_records",Integer.class)).isZero();
 }
 @Test void TC027_protectCardAndItem(){
 long r=reader("S");var c=card(r);long i=item(title("T"),"B");circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"));
 assertThatThrownBy(()->cards.deactivate(admin,c.id())).hasMessage("ACTIVE_ASSOCIATION");
 assertThatThrownBy(()->catalog.removeItem(admin,i)).hasMessage("ACTIVE_ASSOCIATION");
 }
 @Test void referenceCopyCannotBorrow(){
 long r=reader("S");var c=card(r);long t=title("T");long i=item(t,"B");db.update("UPDATE library_items SET loan_eligibility='REFERENCE_ONLY' WHERE item_id=?",i);
 assertThatThrownBy(()->circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"))).hasMessage("ITEM_UNAVAILABLE");
 }
 @Test void activeLoanUniqueConstraint(){
 long r=reader("S");var c=card(r);long i=item(title("T"),"B");circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"));
 assertThatThrownBy(()->repo.insertLoan(r,c.id(),i,1,today,repo.policy(com.example.library.domain.reader.ReaderType.UNDERGRADUATE),repo.fineRule(com.example.library.domain.catalog.ItemType.CHINESE_BOOK))).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
 }
 @Test void cardDeactivateAndReissue(){long r=reader("S");var c=card(r);cards.deactivate(admin,c.id());var next=card(r);assertThat(next.cardNo()).isNotEqualTo(c.cardNo());assertThat(repo.card(c.id()).status()).isEqualTo("INACTIVE");}
 @Test void concurrentReservations()throws Exception{
 long t=title("T");item(t,"B");db.update("UPDATE library_items SET status='ON_LOAN'");
 long a=reader("A"),b=reader("B");card(a);card(b);var pool=Executors.newFixedThreadPool(2);var gate=new CountDownLatch(1);
 try{var x=pool.submit(()->{gate.await();return reservations.reserve(new Actor(Actor.Role.READER,a),new Requests.ReserveBook(t));});
 var y=pool.submit(()->{gate.await();return reservations.reserve(new Actor(Actor.Role.READER,b),new Requests.ReserveBook(t));});gate.countDown();
 assertThat(List.of(x.get(15,TimeUnit.SECONDS).queueNo(),y.get(15,TimeUnit.SECONDS).queueNo())).containsExactlyInAnyOrder(1L,2L);
 }finally{pool.shutdownNow();}
 }
}

