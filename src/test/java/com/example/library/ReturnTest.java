package com.example.library;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.library.application.CirculationService;
import com.example.library.dto.Requests;
import static org.assertj.core.api.Assertions.*;
class ReturnTest extends IntegrationSupport {
 @Autowired CirculationService circulation;
 private long borrowed(){long r=reader("S");var c=card(r);long t=title("T");item(t,"B");return circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B")).id();}
 @Test void TC008_normal(){long loan=borrowed();var result=circulation.returnBook(librarian,new Requests.ReturnBook("B"));assertThat(result.loan().status()).isEqualTo("RETURNED");assertThat(result.fine().amount()).isEqualByComparingTo("0");assertThat(repo.item("B").status()).isEqualTo("AVAILABLE");}
 @Test void TC009_foreign(){assertThatThrownBy(()->circulation.returnBook(librarian,new Requests.ReturnBook("MISSING"))).hasMessage("ITEM_NOT_FOUND");}
 @Test void TC019_noLoan(){item(title("T"),"B");assertThatThrownBy(()->circulation.returnBook(librarian,new Requests.ReturnBook("B"))).hasMessage("LOAN_NOT_FOUND");}
 @Test void TC010_overdue(){borrowed();db.update("UPDATE loans SET borrowed_at=?,due_at=?",today.minusDays(32),today.minusDays(2));var result=circulation.returnBook(librarian,new Requests.ReturnBook("B"));assertThat(result.fine().amount()).isEqualByComparingTo("1.00");assertThat(result.fine().overdueDays()).isEqualTo(2);}
 @Test void TC025_snapshot(){borrowed();db.update("UPDATE loans SET borrowed_at=?,due_at=?",today.minusDays(32),today.minusDays(2));db.update("UPDATE fine_rules SET daily_rate=9 WHERE item_type='CHINESE_BOOK'");var result=circulation.returnBook(librarian,new Requests.ReturnBook("B"));assertThat(result.fine().amount()).isEqualByComparingTo("1.00");}
 @Test void duplicateReturn(){borrowed();circulation.returnBook(librarian,new Requests.ReturnBook("B"));assertThatThrownBy(()->circulation.returnBook(librarian,new Requests.ReturnBook("B"))).hasMessage("LOAN_NOT_FOUND");assertThat(db.queryForObject("SELECT COUNT(*) FROM fine_records",Integer.class)).isEqualTo(1);}
}
