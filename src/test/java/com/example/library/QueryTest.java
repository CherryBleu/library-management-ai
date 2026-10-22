package com.example.library;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.library.application.*;
import com.example.library.dto.Requests;
import com.example.library.domain.admin.Actor;
import static org.assertj.core.api.Assertions.*;
class QueryTest extends IntegrationSupport {
 @Autowired QueryService query; @Autowired CirculationService circulation;
 @Test void TC011_TC021_query(){long r=reader("S");var c=card(r);item(title("Algorithms"),"B");circulation.borrow(librarian,new Requests.BorrowBook(c.cardNo(),"B"));
 var a=new Actor(Actor.Role.READER,r);assertThat(query.loans(a,r)).hasSize(1);assertThat(query.loans(librarian,r)).hasSize(1);
 db.update("UPDATE loans SET borrowed_at=?,due_at=?",today.minusDays(31),today.minusDays(1));assertThat(query.loans(a,r).get(0).status()).isEqualTo("OVERDUE");
 circulation.returnBook(librarian,new Requests.ReturnBook("B"));assertThat(query.loans(a,r).get(0).status()).isEqualTo("RETURNED");
 assertThat(query.books(a,"algo")).hasSize(1);assertThat(query.books(a,"nothing")).isEmpty();
 }
 @Test void TC020_otherReader(){long a=reader("A"),b=reader("B");assertThatThrownBy(()->query.loans(new Actor(Actor.Role.READER,a),b)).hasMessage("FORBIDDEN");}
}
